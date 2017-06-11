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
    'Drafts'        = <any>'Drafts',
    'Revision'      = <any>'Requires revision',
    'Review'        = <any>'In review',
    'Delivery'      = <any>'In delivery',
    'Delivered'     = <any>'Delivered',
    'Rejected'      = <any>'Rejected',
    'Cancelled'     = <any>'Cancelled'
}

export const RequestStatusSidebarOptions: { [option: string]: any; } = {
    'All': {
        'option': StatusSidebarOption.All,
        'action': RequestStatusOptions.Draft,
        'icon': 'assignment'
    },
    'Drafts': {
        'option': StatusSidebarOption.Drafts,
        'action': RequestStatusOptions.Draft,
        'icon': 'drafts'
    },
    'Need_Revision': {
        'option': StatusSidebarOption.Revision,
        'action': RequestReviewStatusOptions.Revision,
        'icon': 'redo'
    },
    'In_Review': {
        'option': StatusSidebarOption.Review,
        'action': RequestReviewStatusOptions.Review,
        'icon': 'assignment_ind'
    },
    'In_Delivery': {
        'option': StatusSidebarOption.Delivery,
        'action': RequestStatusOptions.Delivery,
        'icon': 'directions_bus'
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
