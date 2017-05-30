/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { RequestReviewDecision } from './request-review-decision';
import { User } from '../user/user.model';

export class RequestReviewFeedback {

    id?: string;
    advice?: RequestReviewDecision;
    reviewer?: User;
    date?: Date;
    summary?: string;
    description?: string;

    constructor() {
    }

}
