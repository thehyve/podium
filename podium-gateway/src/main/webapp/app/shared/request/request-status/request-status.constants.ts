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
    'Approved': {
        name: 'Approved',
        order: 4
    },
    'Delivery': {
        name: 'Delivery',
        order: 5
    },
    'Finished' : {
        name: 'Finished',
        order: 6
    }
};

const requestProgressBarStatusOpts: { [status: string]: any; } = {
    'Draft': {
        name: 'Submitted',
        order: 1
    },
    'Validation': {
        name: 'Validation',
        order: 2
    },
    'Revision': {
        name: 'Revision',
        order: 2
    },
    'Review': {
        name: 'Review',
        order: 3
    },
    'Approved': {
        name: 'Approved',
        order: 4
    },
    'Delivery': {
        name: 'Delivery',
        order: 5
    },
    'Finished' : {
        name: 'Finished',
        order: 6
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
    Draft       = <any>'Draft',
    Review      = <any>'Review',
    Approved    = <any>'Approved',
    Delivery    = <any>'Delivery',
    Closed      = <any>'Closed',
    None        = <any>'None',
}

export enum RequestReviewStatusOptions {
    Revision    = <any>'Revision',
    Validation  = <any>'Validation',
    Review      = <any>'Review',
    Closed      = <any>'Closed',
    None        = <any>'None'
}

export enum RequestOverviewStatusOption {
    All                  = <any>'All',
    Draft                = <any>'Draft',
    Validation           = <any>'Validation',
    Review               = <any>'Review',
    Revision             = <any>'Revision',
    Approved             = <any>'Approved',
    Delivery             = <any>'Delivery',
    Delivered            = <any>'Delivered',
    Partially_Delivered  = <any>'Partially_Delivered',
    Rejected             = <any>'Rejected',
    Cancelled            = <any>'Cancelled',
    Closed_Approved      = <any>'Closed_Approved',
    None                 = <any>'None'
}

export type StatusType = RequestStatusOptions | RequestReviewStatusOptions;

export const REQUEST_STATUSES: ReadonlyArray<RequestStatus> = convertNamesToRequestStatuses(requestStatusesOpts);

export const REQUEST_PROGRESSBAR_STATUSES: ReadonlyArray<RequestStatus> = convertNamesToRequestStatuses(requestProgressBarStatusOpts);

export const REQUEST_PROGRESSBAR_STATUSES_MAP: { [token: string]: RequestStatus; }
    = convertToRequestStatusMap(REQUEST_PROGRESSBAR_STATUSES);
