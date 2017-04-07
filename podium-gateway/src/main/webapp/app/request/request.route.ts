/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Routes } from '@angular/router';

import { UserRouteAccessService } from '../shared';

import { requestFormRoute } from './';
import { requestOverviewRoute } from './overview/request-overview.route';
import { requestFormEditRoute } from './form/request-form-edit.route';

let REQUEST_ROUTES = [
    requestFormRoute,
    requestFormEditRoute,
    requestOverviewRoute
];

export const requestRoute: Routes = [{
    path: 'requests',
    children: REQUEST_ROUTES,
    data: {
        authorities: ['ROLE_RESEARCHER']
    },
    canActivate: [ UserRouteAccessService ]
}];
