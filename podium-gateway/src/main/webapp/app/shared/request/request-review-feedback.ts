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
import { PodiumEventMessage } from '../event/podium-event-message';

export class RequestReviewFeedback {

    id?: string;
    uuid?: string;
    reviewer?: User;
    advice?: RequestReviewDecision;
    date?: Date;
    message?: PodiumEventMessage;

    constructor() {
    }

}
