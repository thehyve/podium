/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { User } from '../../shared';

export class Role {

    id?: number;
    /**
     * UUID of the organisation (only for organisation roles).
     */
    organisation?: string;
    /**
     * Authority token, e.g., ROLE_ORGANISATION_ADMIN.
     */
    authority?: string;
    /**
     * UUIDs of users for this role.
     */
    users?: string[];

    constructor() { }

}
