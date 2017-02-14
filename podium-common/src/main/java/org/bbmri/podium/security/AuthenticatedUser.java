package org.bbmri.podium.security;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface AuthenticatedUser {

    UUID getUuid();

    String getName();

    String getPassword();

    Collection<String> getAuthorityNames();

    Map<UUID, Collection<String>> getOrganisationAuthorities();

}
