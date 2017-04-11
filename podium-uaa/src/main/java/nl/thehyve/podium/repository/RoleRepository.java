/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;

import nl.thehyve.podium.domain.Authority;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Role entity.
 */
@SuppressWarnings("unused")
public interface RoleRepository extends JpaRepository<Role,Long> {

    @Query(value = "select distinct role from Role role left join fetch role.users",
        countQuery = "select count(role) from Role role")
    Page<Role> findAllWithUsers(Pageable pageable);

    @Query("select role from Role role left join fetch role.users where role.id =:id")
    Role findOneWithUsers(@Param("id") Long id);

    List<Role> findAllByOrganisation(Organisation organisation);

    @Query("select role from Role role inner join role.authority authority where authority.name = :authorityName")
    Role findByAuthorityName(@Param("authorityName") String authorityName);

    @Query("select role from Role role inner join role.authority authority" +
        " where role.organisation = :organisation and authority.name = :authorityName")
    Role findByOrganisationAndAuthorityName(
        @Param("organisation") Organisation organisation,
        @Param("authorityName") String authorityName);

    List<Role> findAllByAuthority(Authority authority);

    @Query("select count(role)>0 from Role role where role.organisation = :organisation")
    boolean existsByOrganisation(
        @Param("organisation") Organisation organisation
    );
}
