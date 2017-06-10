/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { DeliveryStatus } from './delivery-status.constants';
import { DeliveryOutcome } from './delivery-outcome.constants';
import { RequestType } from '../request/request-type';
import { PodiumEvent } from '../event/podium-event';

export class Delivery {
    uuid?: string;
    status?: DeliveryStatus;
    outcome?: DeliveryOutcome;
    type?: RequestType;
    reference?: string;
    createdDate?: Date;
    lastModifiedDate?: Date;
    historicEvents?: PodiumEvent[];

    constructor() {

    }
}
