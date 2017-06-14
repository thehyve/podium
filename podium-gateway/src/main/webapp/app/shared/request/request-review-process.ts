/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { RequestReviewStatusOptions } from './request-status/request-status.constants';
import { RequestReviewDecision } from './request-review-decision';

export class RequestReviewProcess {

    status: RequestReviewStatusOptions;
    processInstanceId: string;
    decision?: RequestReviewDecision;

    constructor() {
    }

}
