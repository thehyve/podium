package org.bbmri.podium.repository;

import org.bbmri.podium.domain.RequestDetail;

import org.springframework.data.jpa.repository.*;

/**
 * Spring Data JPA repository for the RequestDetail entity.
 */
@SuppressWarnings("unused")
public interface RequestDetailRepository extends JpaRepository<RequestDetail,Long> {

}
