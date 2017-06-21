/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { RequestOverviewPath } from '../../../request/overview/request-overview.constants';
import { UserGroupAuthority } from '../../authority/authority.constants';
export enum StatusSidebarOption {
    'All'                   = <any>'All',
    'Draft'                 = <any>'Draft',
    'Validation'            = <any>'Validation',
    'Revision'              = <any>'Revision',
    'Review'                = <any>'Review',
    'Approved'              = <any>'Approved',
    'Delivery'              = <any>'Delivery',
    'Partially_Delivered'    = <any>'Partially_Delivered',
    'Delivered'             = <any>'Delivered',
    'Rejected'              = <any>'Rejected',
    'Cancelled'             = <any>'Cancelled'
}

export const RequestStatusSidebarOptions: { [option: string]: any; } = {
    'All': {
        'option': StatusSidebarOption.All,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
            UserGroupAuthority.Reviewer
        ],
        'icon': 'assignment'
    },
    'Draft': {
        'option': StatusSidebarOption.Draft,
        'includeFor': [
            UserGroupAuthority.Requester
        ],
        'icon': 'drafts'
    },
    'Validation': {
        'option': StatusSidebarOption.Validation,
        'group': 'Review',
        'groupOrder': 1,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'visibility'
    },
    'Revision': {
        'option': StatusSidebarOption.Revision,
        'group': 'Review',
        'groupOrder': 2,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'redo'
    },
    'Review': {
        'option': StatusSidebarOption.Review,
        'group': 'Review',
        'groupOrder': 3,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
            UserGroupAuthority.Reviewer
        ],
        'icon': 'assignment_ind'
    },
    'Approved': {
        'option': StatusSidebarOption.Approved,
        'group': 'Approved',
        'groupOrder': 1,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'thumb_up'
    },
    'Delivery': {
        'option': StatusSidebarOption.Delivery,
        'group': 'Delivery',
        'groupOrder': 1,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'local_shipping'
    },
    'Partially_Delivered': {
        'option': StatusSidebarOption.Partially_Delivered,
        'group': 'Closed',
        'groupOrder': 1,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'space_bar'
    },
    'Delivered': {
        'option': StatusSidebarOption.Delivered,
        'group': 'Closed',
        'groupOrder': 2,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'inbox'
    },
    'Rejected': {
        'option': StatusSidebarOption.Rejected,
        'group': 'Closed',
        'groupOrder': 3,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'highlight_off'
    },
    'Cancelled': {
        'option': StatusSidebarOption.Cancelled,
        'group': 'Closed',
        'groupOrder': 4,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
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
