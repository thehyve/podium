/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { RequestType } from '../../../shared/request/request-type';
import { Role } from '../../../shared/role/role.model';

export class Organisation {

    public id?: number;
    uuid?: string;
    name?: string;
    shortName?: string;
    activated?: boolean;
    organisationUuid?: string;
    public roles?: Role[];
    public requestTypes?: RequestType[];

    constructor(jsonResponse? : any) {
        jsonResponse ? Object.assign(this, jsonResponse) : this;
    }

}
