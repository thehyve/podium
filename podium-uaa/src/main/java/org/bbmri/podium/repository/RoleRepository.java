package org.bbmri.podium.repository;

import org.bbmri.podium.domain.Authority;
import org.bbmri.podium.domain.Organisation;
import org.bbmri.podium.domain.Role;

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

    List<Role> findAllByAuthority(Authority authority);
}
