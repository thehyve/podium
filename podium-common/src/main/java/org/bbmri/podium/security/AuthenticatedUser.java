package org.bbmri.podium.security;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface AuthenticatedUser extends Principal {

    UUID getUuid();

    String getName();

    String getPassword();

    Collection<String> getAuthorityNames();

    Map<UUID, Collection<String>> getOrganisationAuthorities();

}
