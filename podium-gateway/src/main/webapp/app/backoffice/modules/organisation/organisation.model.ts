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

export interface IOrganisation {
    hasRequestTypes(requestTypes: RequestType[]): boolean;
}

export class Organisation implements IOrganisation {

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

    hasRequestTypes(requestTypes) {

        let found = false;

        for (let requestType of requestTypes) {
            if (this.requestTypes.indexOf(requestType) >= 0) {
                found = true;
                break;
            }
        }

        return found;
    }
}
