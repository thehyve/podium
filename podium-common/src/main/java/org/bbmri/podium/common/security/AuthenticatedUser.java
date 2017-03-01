/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package org.bbmri.podium.common.security;

import org.bbmri.podium.common.IdentifiableUser;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface AuthenticatedUser extends Principal, IdentifiableUser {

    UUID getUuid();

    String getName();

    String getPassword();

    Collection<String> getAuthorityNames();

    Map<UUID, Collection<String>> getOrganisationAuthorities();

}
