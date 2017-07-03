/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { UserGroupAuthority } from '../../authority/authority.constants';
import { RequestOverviewStatusOption } from '../request-status/request-status.constants';

export const RequestStatusSidebarOptions: { [option: string]: any; } = {
    'All': {
        'option': RequestOverviewStatusOption.All,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
            UserGroupAuthority.Reviewer
        ],
        'icon': 'assignment'
    },
    'Draft': {
        'option': RequestOverviewStatusOption.Draft,
        'includeFor': [
            UserGroupAuthority.Requester
        ],
        'icon': 'drafts'
    },
    'Validation': {
        'option': RequestOverviewStatusOption.Validation,
        'group': 'Review',
        'groupOrder': 1,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'visibility'
    },
    'Revision': {
        'option': RequestOverviewStatusOption.Revision,
        'group': 'Review',
        'groupOrder': 2,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'redo'
    },
    'Review': {
        'option': RequestOverviewStatusOption.Review,
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
        'option': RequestOverviewStatusOption.Approved,
        'group': 'Approved',
        'groupOrder': 1,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'thumb_up'
    },
    'Delivery': {
        'option': RequestOverviewStatusOption.Delivery,
        'group': 'Delivery',
        'groupOrder': 1,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'local_shipping'
    },
    'Partially_Delivered': {
        'option': RequestOverviewStatusOption.Partially_Delivered,
        'group': 'Closed',
        'groupOrder': 1,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'space_bar'
    },
    'Delivered': {
        'option': RequestOverviewStatusOption.Delivered,
        'group': 'Closed',
        'groupOrder': 2,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'inbox'
    },
    'Rejected': {
        'option': RequestOverviewStatusOption.Rejected,
        'group': 'Closed',
        'groupOrder': 3,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'highlight_off'
    },
    'Cancelled': {
        'option': RequestOverviewStatusOption.Cancelled,
        'group': 'Closed',
        'groupOrder': 4,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'cancel'
    },
    'Closed_Approved': {
        'option': RequestOverviewStatusOption.Closed_Approved,
        'group': 'Closed',
        'groupOrder': 5,
        'includeFor': [
            UserGroupAuthority.Requester,
            UserGroupAuthority.Coordinator,
        ],
        'icon': 'thumbs_up_down'
    },
};

function convertNamesToRequestOverviewStatusOptions(options: { [status: string]: any; }): Array<RequestOverviewStatusOption> {
    let result: Array<RequestOverviewStatusOption> = [];
    for (const status in options) {
        if (options.hasOwnProperty(status)) {
            result.push(options[status]);
        }
    }
    return result;
}

export const StatusSidebarOptionsCollection = convertNamesToRequestOverviewStatusOptions(RequestStatusSidebarOptions);
