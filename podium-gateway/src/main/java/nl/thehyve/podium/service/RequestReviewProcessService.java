/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.domain.RequestReviewProcess;
import nl.thehyve.podium.repository.RequestReviewProcessRepository;
import nl.thehyve.podium.repository.search.RequestReviewProcessSearchRepository;
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

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class RequestReviewProcessService {

    public enum ReviewTask {
        Validation("validation"),
        Review("review"),
        Revision("revision");

        private String taskId;

        public String getTaskId() {
            return taskId;
        }

        ReviewTask(String taskId) {
            this.taskId = taskId;
        }
    }

    public enum ReviewVariable {
        ValidationPassed("validation_passed"),
        RequestRevision("request_revision"),
        RequestApproved("request_approved");

        private String variableName;

        public String getVariableName() {
            return variableName;
        }

        ReviewVariable(String variableName) {
            this.variableName = variableName;
        }
    }

    private static final Map<RequestReviewStatus, ReviewTask> taskForStatus = new HashMap<>(3);
    {
        taskForStatus.put(RequestReviewStatus.Validation, ReviewTask.Validation);
        taskForStatus.put(RequestReviewStatus.Review, ReviewTask.Review);
        taskForStatus.put(RequestReviewStatus.Revision, ReviewTask.Revision);
    }

    public static final String CURRENT_PROCESS_VERSION = "podium_request_review_001";

    Logger log = LoggerFactory.getLogger(RequestReviewProcessService.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RequestReviewProcessRepository requestReviewProcessRepository;

    @Autowired
    private RequestReviewProcessSearchRepository requestReviewProcessSearchRepository;

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
     * @param requestId
     * @return the current task if it exists, null otherwise.
     */
    private Task findTaskByRequestId(String requestId, String taskId) {
        return taskService.createTaskQuery().processInstanceId(requestId)
            .active()
            .taskDefinitionKey(taskId)
            .singleResult();
    }

    /**
     * Copy properties from the process variables to the request review entity.
     */
    private RequestReviewProcess updateRequestReviewProcess(RequestReviewProcess requestReviewProcess) {
        // Update the request review object with the process variables.
        HistoricProcessInstance instance = findProcessInstance(requestReviewProcess.getProcessInstanceId());
        Map<String, Object> variables = instance.getProcessVariables();
        requestReviewProcess.setStatus((RequestReviewStatus) variables.get("status"));
        requestReviewProcess.setDecision((ReviewProcessOutcome) variables.get("decision"));
        requestReviewProcess = requestReviewProcessRepository.save(requestReviewProcess);
        // save to elastic search as well
        requestReviewProcessSearchRepository.save(requestReviewProcess);
        return requestReviewProcess;
    }

    private RequestReviewProcess completeCurrentTask(AuthenticatedUser user, RequestReviewProcess requestReviewProcess) {
        RequestReviewStatus status = requestReviewProcess.getStatus();
        if (status == RequestReviewStatus.None) {
            throw new RuntimeException("Invalid status.");
        }
        ReviewTask reviewTask = taskForStatus.get(status);
        Task task = findTaskByRequestId(requestReviewProcess.getProcessInstanceId(), reviewTask.getTaskId());
        if (task == null) {
            throw new ResourceNotFound("Task not found for status " + status.name() + " (" + reviewTask.getTaskId() + ")");
        }
        taskService.addUserIdentityLink(task.getId(), user.getUuid().toString(), IdentityLinkType.PARTICIPANT);
        if (task.getDelegationState() == DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        return updateRequestReviewProcess(requestReviewProcess);
    }

    private void setVariable(RequestReviewProcess requestReviewProcess, ReviewVariable variable, Object value) {
        runtimeService.setVariable(
            requestReviewProcess.getProcessInstanceId(),
            variable.getVariableName(),
            value);
    }

    public RequestReviewProcess submitForValidation(AuthenticatedUser user, RequestReviewProcess requestReviewProcess) throws ActionNotAllowed {
        if (requestReviewProcess.getStatus() == RequestReviewStatus.Revision) {
            return completeCurrentTask(user, requestReviewProcess);
        }
        throw ActionNotAllowed.forStatus(requestReviewProcess.getStatus());
    }

    public RequestReviewProcess submitForReview(AuthenticatedUser user, RequestReviewProcess requestReviewProcess) throws ActionNotAllowed {
        if (requestReviewProcess.getStatus() == RequestReviewStatus.Validation) {
            runtimeService.setVariable(requestReviewProcess.getProcessInstanceId(), "validation_passed", Boolean.TRUE);
            return completeCurrentTask(user, requestReviewProcess);
        }
        throw ActionNotAllowed.forStatus(requestReviewProcess.getStatus());
    }

    public RequestReviewProcess requestRevision(AuthenticatedUser user, RequestReviewProcess requestReviewProcess) throws ActionNotAllowed {
        if (requestReviewProcess.getStatus() == RequestReviewStatus.Validation) {
            setVariable(requestReviewProcess, ReviewVariable.ValidationPassed, Boolean.FALSE);
            setVariable(requestReviewProcess, ReviewVariable.RequestRevision, Boolean.TRUE);
            return completeCurrentTask(user, requestReviewProcess);
        } else if (requestReviewProcess.getStatus() == RequestReviewStatus.Review) {
            setVariable(requestReviewProcess, ReviewVariable.RequestApproved, Boolean.FALSE);
            setVariable(requestReviewProcess, ReviewVariable.RequestRevision, Boolean.TRUE);
            return completeCurrentTask(user, requestReviewProcess);
        }
        throw ActionNotAllowed.forStatus(requestReviewProcess.getStatus());
    }

    public RequestReviewProcess reject(AuthenticatedUser user, RequestReviewProcess requestReviewProcess) throws ActionNotAllowed {
        if (requestReviewProcess.getStatus() == RequestReviewStatus.Validation) {
            setVariable(requestReviewProcess, ReviewVariable.ValidationPassed, Boolean.FALSE);
            setVariable(requestReviewProcess, ReviewVariable.RequestRevision, Boolean.FALSE);
            return completeCurrentTask(user, requestReviewProcess);
        } else if (requestReviewProcess.getStatus() == RequestReviewStatus.Review) {
            setVariable(requestReviewProcess, ReviewVariable.RequestApproved, Boolean.FALSE);
            setVariable(requestReviewProcess, ReviewVariable.RequestRevision, Boolean.FALSE);
            return completeCurrentTask(user, requestReviewProcess);
        }
        throw ActionNotAllowed.forStatus(requestReviewProcess.getStatus());
    }

    public RequestReviewProcess approve(AuthenticatedUser user, RequestReviewProcess requestReviewProcess) throws ActionNotAllowed {
        if (requestReviewProcess.getStatus() == RequestReviewStatus.Review) {
            setVariable(requestReviewProcess, ReviewVariable.RequestApproved, Boolean.TRUE);
            setVariable(requestReviewProcess, ReviewVariable.RequestRevision, Boolean.FALSE);
            return completeCurrentTask(user, requestReviewProcess);
        }
        throw ActionNotAllowed.forStatus(requestReviewProcess.getStatus());
    }

    @Timed
    public RequestReviewProcess start(AuthenticatedUser user) {
        log.info("Creating request review process instance for user {}", user.getName());

        // start new process instance
        Map<String, Object> values = new HashMap<>();
        values.put("initiator", user.getUuid().toString());

        ProcessInstance newInstance = runtimeService.startProcessInstanceByKey(
            CURRENT_PROCESS_VERSION, values);
        String processInstanceId = newInstance.getProcessInstanceId();
        runtimeService.addUserIdentityLink(processInstanceId, user.getUuid().toString(), IdentityLinkType.STARTER);
        runtimeService.addUserIdentityLink(processInstanceId, user.getUuid().toString(), IdentityLinkType.OWNER);
        log.info("New process instance started: {}", processInstanceId);

        RequestReviewProcess requestReviewProcess = new RequestReviewProcess();
        requestReviewProcess.setProcessInstanceId(processInstanceId);
        requestReviewProcess = requestReviewProcessRepository.save(requestReviewProcess);

        return updateRequestReviewProcess(requestReviewProcess);
    }

}
