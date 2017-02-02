package org.bbmri.podium.repository;

import org.bbmri.podium.domain.Organisation;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Organisation entity.
 */
@SuppressWarnings("unused")
public interface OrganisationRepository extends JpaRepository<Organisation,Long> {

}
