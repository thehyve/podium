package nl.thehyve.podium.service;

import nl.thehyve.podium.common.enumeration.DeliveryStatus;
import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.event.StatusUpdateEvent;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.domain.DeliveryProcess;
import nl.thehyve.podium.domain.PodiumEvent;
import nl.thehyve.podium.domain.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * Service for publishing and persisting status update events.
 */
@Service
class StatusUpdateEventService {

    private final Logger log = LoggerFactory.getLogger(StatusUpdateEventService.class);

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private EntityManager entityManager;


    @Transactional
    protected void persistAndPublishEvent(Request request, StatusUpdateEvent event) {
        PodiumEvent historicEvent = new PodiumEvent(event);
        entityManager.persist(historicEvent);
        request.addHistoricEvent(historicEvent);
        entityManager.persist(request);
        log.debug("Publish event: {}", event);
        publisher.publishEvent(event);
    }

    void publishStatusUpdate(AuthenticatedUser user, OverviewStatus sourceStatus, Request request, MessageRepresentation message) {
        StatusUpdateEvent event =
            new StatusUpdateEvent<>(user, sourceStatus, request.getOverviewStatus(), request.getUuid(), message);
        persistAndPublishEvent(request, event);
    }

    @Transactional
    protected void persistAndPublishDeliveryEvent(DeliveryProcess deliveryProcess, StatusUpdateEvent event) {
        PodiumEvent historicEvent = new PodiumEvent(event);
        entityManager.persist(historicEvent);
        deliveryProcess.addHistoricEvent(historicEvent);
        entityManager.persist(deliveryProcess);
        log.debug("Publish delivery event: {}", event);
        publisher.publishEvent(event);
    }

    void publishDeliveryStatusUpdate(AuthenticatedUser user, DeliveryStatus sourceStatus, Request request, DeliveryProcess deliveryProcess, MessageRepresentation message) {
        StatusUpdateEvent event =
            new StatusUpdateEvent<>(user, sourceStatus, deliveryProcess.getStatus(), request.getUuid(), deliveryProcess.getUuid(), message);
        persistAndPublishDeliveryEvent(deliveryProcess, event);
    }

}
