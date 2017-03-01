package org.bbmri.podium.repository;

import org.bbmri.podium.domain.Request;

import org.bbmri.podium.domain.enumeration.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the Request entity.
 */
@SuppressWarnings("unused")
public interface RequestRepository extends JpaRepository<Request,Long> {
    List<Request> findAllByRequesterAndStatus(UUID requester, RequestStatus status);

}
