/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Request } from './request';
import { Organisation } from '../../entities/organisation/organisation.model';
import { RequestStatus } from './request-status';
import { User } from '../user/user.model';
import { RequestDetail } from './request-detail';

export class RequestBase implements Request {
    dateCreated?: Date;
    dateLastModified?: Date;
    uuid?: string;
    id?: string;
    status?: RequestStatus;
    requestDetail?: RequestDetail;
    organisations?: Organisation[];
    requester?: User;

    constructor(dateCreated?: Date,
                dateLastModified?: Date,
                uuid?: string,
                id?: string,
                status?: RequestStatus,
                requestDetail?: RequestDetail,
                organisations?: Organisation[],
                requester?: User
    ) {
        this.dateCreated = dateCreated ? dateCreated : new Date();
        this.dateLastModified = dateLastModified ? dateLastModified : new Date();
        this.uuid = uuid ? uuid : null;
        this.id = id ? id : null;
        this.status = status ? status : null;
        this.requestDetail = requestDetail ? requestDetail : new RequestDetail();
        this.organisations = organisations ? organisations : [];
        this.requester = requester ? requester : null;
    }
}
