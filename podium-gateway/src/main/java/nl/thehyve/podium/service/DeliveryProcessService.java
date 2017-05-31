/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.enumeration.*;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.domain.DeliveryProcess;
import nl.thehyve.podium.repository.DeliveryProcessRepository;
import nl.thehyve.podium.repository.search.DeliveryProcessSearchRepository;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.DelegationState;
import org.flowable.engine.task.IdentityLinkType;
import org.flowable.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Timed
public class DeliveryProcessService {

    public enum DeliveryTask {
        Preparation("preparation"),
        Released("released");

        private String taskId;

        public String getTaskId() {
            return taskId;
        }

        DeliveryTask(String taskId) {
            this.taskId = taskId;
        }
    }

    public enum DeliveryVariable {
        Release("release"),
        Received("received");

        private String variableName;

        public String getVariableName() {
            return variableName;
        }

        DeliveryVariable(String variableName) {
            this.variableName = variableName;
        }
    }

    private static final Map<DeliveryStatus, DeliveryTask> taskForStatus = new HashMap<>(3);
    {
        taskForStatus.put(DeliveryStatus.Preparation, DeliveryTask.Preparation);
        taskForStatus.put(DeliveryStatus.Released, DeliveryTask.Released);
    }

    public static final String CURRENT_DELIVERY_PROCESS_VERSION = "podium_delivery_001";

    Logger log = LoggerFactory.getLogger(DeliveryProcessService.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private DeliveryProcessRepository deliveryProcessRepository;

    @Autowired
    private DeliveryProcessSearchRepository deliveryProcessSearchRepository;

    /**
     * Finds request.
     * @param processInstanceId
     * @return the current request if it exists; null otherwise.
     */
    private HistoricProcessInstance findProcessInstance(String processInstanceId) {
        return historyService
            .createHistoricProcessInstanceQuery()
            .includeProcessVariables()
            .processInstanceId(processInstanceId)
            .singleResult();
    }

    /**
     * Finds current task. Assumes that at most one task is currently active.
     * @param processInstanceId
     * @return the current task if it exists, null otherwise.
     */
    private Task findTaskByRequestId(String processInstanceId, String taskId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId)
            .active()
            .taskDefinitionKey(taskId)
            .singleResult();
    }

    /**
     * Copy properties from the process variables to the delivery process entity.
     */
    private DeliveryProcess updateDeliveryProcess(DeliveryProcess deliveryProcess) {
        // Update the delivery process object with the process variables.
        HistoricProcessInstance instance = findProcessInstance(deliveryProcess.getProcessInstanceId());
        Map<String, Object> variables = instance.getProcessVariables();
        deliveryProcess.setStatus((DeliveryStatus) variables.get("status"));
        deliveryProcess.setOutcome((DeliveryProcessOutcome) variables.get("outcome"));
        deliveryProcess = deliveryProcessRepository.save(deliveryProcess);
        // save to elastic search as well
        deliveryProcessSearchRepository.save(deliveryProcess);
        return deliveryProcess;
    }

    private DeliveryProcess completeCurrentTask(AuthenticatedUser user, DeliveryProcess deliveryProcess) {
        DeliveryStatus status = deliveryProcess.getStatus();
        if (status == DeliveryStatus.None) {
            throw new RuntimeException("Invalid status.");
        }
        DeliveryTask deliveryTask = taskForStatus.get(status);
        Task task = findTaskByRequestId(deliveryProcess.getProcessInstanceId(), deliveryTask.getTaskId());
        if (task == null) {
            throw new ResourceNotFound("Task not found for status " + status.name() + " (" + deliveryTask.getTaskId() + ")");
        }
        taskService.addUserIdentityLink(task.getId(), user.getUuid().toString(), IdentityLinkType.PARTICIPANT);
        if (task.getDelegationState() == DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        return updateDeliveryProcess(deliveryProcess);
    }

    private void setVariable(DeliveryProcess deliveryProcess, DeliveryVariable variable, Object value) {
        runtimeService.setVariable(
            deliveryProcess.getProcessInstanceId(),
            variable.getVariableName(),
            value);
    }

    /**
     * Completes the Preparation task and forwards to Released for the specified delivery process.
     * @param user the current user.
     * @param deliveryProcess the delivery process object.
     * @return the updated delivery process object.
     * @throws ActionNotAllowed iff the current status is not {@link DeliveryStatus#Preparation}.
     */
    public DeliveryProcess release(AuthenticatedUser user, DeliveryProcess deliveryProcess) throws ActionNotAllowed {
        if (deliveryProcess.getStatus() == DeliveryStatus.Preparation) {
            setVariable(deliveryProcess, DeliveryVariable.Release, Boolean.TRUE);
            return completeCurrentTask(user, deliveryProcess);
        }
        throw ActionNotAllowed.forStatus(deliveryProcess.getStatus());
    }

    /**
     * Completes the Released task and forwards to Received for the specified delivery process, which will
     * end the process and set the outcome to Received.
     * @param user the current user.
     * @param deliveryProcess the delivery process object.
     * @return the updated delivery process object.
     * @throws ActionNotAllowed iff the current status is not {@link DeliveryStatus#Released}.
     */
    public DeliveryProcess received(AuthenticatedUser user, DeliveryProcess deliveryProcess) throws ActionNotAllowed {
        if (deliveryProcess.getStatus() == DeliveryStatus.Released) {
            setVariable(deliveryProcess, DeliveryVariable.Received, Boolean.TRUE);
            return completeCurrentTask(user, deliveryProcess);
        }
        throw ActionNotAllowed.forStatus(deliveryProcess.getStatus());
    }

    /**
     * Completes the Preparation or Released task and cancels the specified delivery process, which will
     * end the process and set the outcome to Cancelled.
     * @param user the current user.
     * @param deliveryProcess the delivery process object.
     * @return the updated delivery process object.
     * @throws ActionNotAllowed iff the current status is not {@link DeliveryStatus#Preparation} or {@link DeliveryStatus#Released}.
     */
    public DeliveryProcess cancel(AuthenticatedUser user, DeliveryProcess deliveryProcess) throws ActionNotAllowed {
        if (deliveryProcess.getStatus() == DeliveryStatus.Released) {
            setVariable(deliveryProcess, DeliveryVariable.Received, Boolean.FALSE);
            return completeCurrentTask(user, deliveryProcess);
        } else if (deliveryProcess.getStatus() == DeliveryStatus.Preparation) {
            setVariable(deliveryProcess, DeliveryVariable.Release, Boolean.FALSE);
            return completeCurrentTask(user, deliveryProcess);
        }
        throw ActionNotAllowed.forStatus(deliveryProcess.getStatus());
    }

    /**
     * Start a delivery process for the specified request type.
     * Starts a process instance, associates the current user with that instance,
     * sets the request type and returns a delivery process object.
     *
     * @param user the current user.
     * @param type the request type.
     * @return the delivery process object.
     */
    public DeliveryProcess start(@NotNull AuthenticatedUser user, @NotNull RequestType type) {
        log.info("Creating delivery process instance for user {}, type {}", user.getName(), type);

        // start new process instance
        Map<String, Object> values = new HashMap<>();
        values.put("initiator", user.getUuid().toString());

        ProcessInstance newInstance = runtimeService.startProcessInstanceByKey(
            CURRENT_DELIVERY_PROCESS_VERSION, values);
        String processInstanceId = newInstance.getProcessInstanceId();
        runtimeService.addUserIdentityLink(processInstanceId, user.getUuid().toString(), IdentityLinkType.STARTER);
        runtimeService.addUserIdentityLink(processInstanceId, user.getUuid().toString(), IdentityLinkType.OWNER);
        log.info("New process instance started: {}", processInstanceId);

        DeliveryProcess deliveryProcess = new DeliveryProcess();
        deliveryProcess.setType(type);
        deliveryProcess.setProcessInstanceId(processInstanceId);
        deliveryProcess = deliveryProcessRepository.save(deliveryProcess);

        return updateDeliveryProcess(deliveryProcess);
    }

}
