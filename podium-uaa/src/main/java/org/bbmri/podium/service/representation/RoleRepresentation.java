package org.bbmri.podium.service.representation;

import org.bbmri.podium.domain.Role;
import org.bbmri.podium.domain.User;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RoleRepresentation {

    private Long id;

    private UUID organisation;

    private String authority;

    private Set<UUID> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getOrganisation() {
        return organisation;
    }

    public void setOrganisation(UUID organisation) {
        this.organisation = organisation;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Set<UUID> getUsers() {
        return users;
    }

    public void setUsers(Set<UUID> users) {
        this.users = users;
    }

    public RoleRepresentation() {
    }

    public RoleRepresentation(Role role) {
        this.id = role.getId();
        this.organisation = role.getOrganisation() != null ? role.getOrganisation().getUuid() : null;
        this.authority = role.getAuthority().getName();
        this.users = role.getUsers().stream().map(User::getUuid).collect(Collectors.toSet());
    }

}
