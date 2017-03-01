/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package org.bbmri.podium.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class AuthorityConstants {

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String PODIUM_ADMIN                 = "ROLE_PODIUM_ADMIN";
    public static final String BBMRI_ADMIN                  = "ROLE_BBMRI_ADMIN";
    public static final String ORGANISATION_ADMIN           = "ROLE_ORGANISATION_ADMIN";
    public static final String ORGANISATION_COORDINATOR     = "ROLE_ORGANISATION_COORDINATOR";
    public static final String REVIEWER                     = "ROLE_REVIEWER";
    public static final String RESEARCHER                   = "ROLE_RESEARCHER";

    public static final Set<String> ORGANISATION_AUTHORITIES = new HashSet<>(Arrays.asList(
        ORGANISATION_ADMIN,
        ORGANISATION_COORDINATOR,
        REVIEWER
    ));

    public static boolean isOrganisationAuthority(String name) {
        return ORGANISATION_AUTHORITIES.contains(name);
    }

}
