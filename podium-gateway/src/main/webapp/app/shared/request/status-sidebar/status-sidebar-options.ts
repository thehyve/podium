/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { RequestStatusOptions, RequestReviewStatusOptions } from '../request-status/request-status.constants';

export enum StatusSidebarOption {
    'All'           = <any>'All',
    'Draft'         = <any>'Draft',
    'Validation'    = <any>'Validation',
    'Revision'      = <any>'Revision',
    'Review'        = <any>'Review',
    'Approved'      = <any>'Approved',
    'Delivery'      = <any>'Delivery',
    'Delivered'     = <any>'Delivered',
    'Rejected'      = <any>'Rejected',
    'Cancelled'     = <any>'Cancelled'
}

export const RequestStatusSidebarOptions: { [option: string]: any; } = {
    'All': {
        'option': StatusSidebarOption.All,
        'icon': 'assignment'
    },
    'Draft': {
        'option': StatusSidebarOption.Draft,
        'icon': 'drafts'
    },
    'Validation': {
        'option': StatusSidebarOption.Validation,
        'icon': 'visibility'
    },
    'Revision': {
        'option': StatusSidebarOption.Revision,
        'icon': 'redo'
    },
    'Review': {
        'option': StatusSidebarOption.Review,
        'icon': 'assignment_ind'
    },
    'Approved': {
        'option': StatusSidebarOption.Approved,
        'icon': 'thumb_up'
    },
    'Delivery': {
        'option': StatusSidebarOption.Delivery,
        'icon': 'local_shipping'
    },
    'Delivered': {
        'option': StatusSidebarOption.Delivered,
        'icon': 'inbox'
    },
    'Rejected': {
        'option': StatusSidebarOption.Rejected,
        'icon': 'highlight_off'
    },
    'Cancelled': {
        'option': StatusSidebarOption.Cancelled,
        'icon': 'cancel'
    },
};

function convertNamesToStatusSidebarOptions(options: { [status: string]: any; }): Array<StatusSidebarOption> {
    let result: Array<StatusSidebarOption> = [];
    for (const status in options) {
        if (options.hasOwnProperty(status)) {
            result.push(options[status]);
        }
    }
    return result;
}

export const StatusSidebarOptionsCollection = convertNamesToStatusSidebarOptions(RequestStatusSidebarOptions);
