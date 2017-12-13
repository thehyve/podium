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
import { RequestDetail } from './request-detail';
import { RequestOverviewStatusOption } from './request-status/request-status.constants';
import { RequestReviewProcess } from './request-review-process';
import { PodiumEvent } from '../event/podium-event';
import { ReviewRound } from './review-round';
import { User } from '../user/user.model';
import { Organisation } from '../organisation/organisation.model';

export class RequestBase implements Request {

    dateCreated?: Date = new Date();
    dateLastModified?: Date = new Date();
    uuid?: string;
    id?: string;
    status?: RequestOverviewStatusOption;
    revisionDetail?: RequestDetail = new RequestDetail();
    requestDetail?: RequestDetail = new RequestDetail();
    requestReview?: RequestReviewProcess;
    reviewRound?: ReviewRound;
    organisations?: Organisation[] = [];
    latestEvent?: PodiumEvent;
    relatedRequests?: RequestBase[] = [];
    hasAttachmentsTypes?: boolean = false;
    requester?: User;

    constructor() {
    }

}
