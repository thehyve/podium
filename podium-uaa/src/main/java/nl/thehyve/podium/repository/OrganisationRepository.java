/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;

import nl.thehyve.podium.domain.Organisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

/**
 * Spring Data JPA repository for the Organisation entity.
 */
@SuppressWarnings("unused")
public interface OrganisationRepository extends JpaRepository<Organisation,Long> {

    Organisation findByUuid(UUID uuid);

    Page<Organisation> findAllByDeletedFalse(Pageable pageable);

    Long countByDeletedFalse();

    Organisation findByIdAndDeletedFalse(Long id);

    Organisation findByUuidAndDeletedFalse(UUID uuid);

    Organisation findByShortNameAndDeletedFalse(String shortName);

    Page<Organisation> findAllByActivatedTrueAndDeletedFalse(Pageable pageable);

    Page<Organisation> findAllByActivatedTrueAndDeletedFalseAndUuidIn(Collection<UUID> organisationUuids, Pageable pageable);

}
