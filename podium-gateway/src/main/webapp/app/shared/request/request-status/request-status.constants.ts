/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { RequestStatus } from './request-status';

const requestStatusesOpts: { [status: string]: any; } = {
    'Draft': {
        name: 'Submitted',
        order: 1
    },
    'Validation': {
        name: 'Validation',
        order: 2
    },
    'Review': {
        name: 'Review',
        order: 3
    },
    'Delivery': {
        name: 'Delivery',
        order: 4
    },
    'Return' : {
        name: 'Return',
        order: 5
    }
};

const requestReviewStatusesOpts: { [status: string]: any; } = {
    'Validation': {
        name: 'Validation',
        order: 2
    },
    'Review': {
        name: 'Review',
        order: 3
    }
};

function convertNamesToRequestStatuses(names: { [status: string]: any; }): Array<RequestStatus> {
    let result: Array<RequestStatus> = [];
    for (const status in names) {
        if (names.hasOwnProperty(status)) {
            result.push({status: status, name: names[status].name, order: names[status].order});
        }
    }
    return result;
}

function convertToRequestStatusMap(requestStatuses: ReadonlyArray<RequestStatus>): { [token: string]: RequestStatus; } {
    let result: { [status: string]: RequestStatus; } = {};
    for (let requestStatus of requestStatuses) {
        if (requestStatuses.indexOf(requestStatus) > -1) {
            result[requestStatus.status] = requestStatus;
        }
    }
    return result;
}

export enum RequestStatusOptions {
    Draft,
    Review,
    Delivery
}

export enum RequestReviewStatusOptions {
    Revision,
    Validation,
    Review,
    Closed,
    None
}

export const REQUEST_STATUSES: ReadonlyArray<RequestStatus> = convertNamesToRequestStatuses(requestStatusesOpts);

export const REQUEST_REVIEW_STATUSES: ReadonlyArray<RequestStatus> = convertNamesToRequestStatuses(requestStatusesOpts);

export const REQUEST_STATUSES_MAP: { [token: string]: RequestStatus; } = convertToRequestStatusMap(REQUEST_STATUSES);

export const REQUEST_REVIEW_STATUSES_MAP: { [token: string]: RequestStatus; } = convertToRequestStatusMap(REQUEST_REVIEW_STATUSES);
