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
import { Organisation } from '../../backoffice/modules/organisation/organisation.model';
import { RequestStatus } from './request-status';
import { User } from '../user/user.model';
import { RequestDetail } from './request-detail';

export class RequestBase implements Request {
    public dateCreated?: Date;
    public dateLastModified?: Date;

    public uuid?: string;
    public id?: string;
    public status?: RequestStatus;
    public requestDetail?: RequestDetail;
    public organisations?: Organisation[];
    public requester?: User;

    constructor() {

    }
}
