/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;

import nl.thehyve.podium.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the User entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneByDeletedIsFalseAndActivationKey(String activationKey);

    List<User> findAllByDeletedIsFalseAndEmailVerifiedIsFalseAndCreatedDateBefore(ZonedDateTime dateTime);

    Optional<User> findOneByDeletedIsFalseAndResetKey(String resetKey);

    Optional<User> findOneByDeletedIsFalseAndEmail(String email);

    Optional<User> findOneByDeletedIsFalseAndLogin(String login);

    Optional<User> findOneByDeletedIsFalseAndUuid(UUID uuid);

    @Query(value = "select distinct user from User user left join fetch user.roles r left join fetch r.authority where user.deleted = false",
        countQuery = "select count(user) from User user where user.deleted = false")
    Page<User> findAllWithAuthorities(Pageable pageable);

}
