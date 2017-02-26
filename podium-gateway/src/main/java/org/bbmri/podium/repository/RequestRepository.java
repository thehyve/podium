package org.bbmri.podium.repository;

import org.bbmri.podium.domain.Request;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Request entity.
 */
@SuppressWarnings("unused")
public interface RequestRepository extends JpaRepository<Request,Long> {

    @Query("select distinct request from Request request left join fetch request.organisations left join fetch request.attachments")
    List<Request> findAllWithEagerRelationships();

    @Query("select request from Request request left join fetch request.organisations left join fetch request.attachments where request.id =:id")
    Request findOneWithEagerRelationships(@Param("id") Long id);

}
