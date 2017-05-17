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
import { requestFormEditRoute } from './form/request-form-edit.route';
import {
    requestOverviewRoute, organisationRequestOverviewRoute,
    reviewerRequestOverviewRoute
} from './overview/request-overview.route';
import { requestMainDetailRoute } from './main-detail/request-main-detail.route';

let defaultRoute = {
    path: '',
    redirectTo: 'overview',
    pathMatch: 'full'
};

let REQUEST_ROUTES = [
    requestFormRoute,
    requestFormEditRoute,
    requestOverviewRoute,
    reviewerRequestOverviewRoute,
    organisationRequestOverviewRoute,
    requestMainDetailRoute,
    defaultRoute
];

export const requestRoute: Routes = [
    {
        path: 'requests',
        children: REQUEST_ROUTES,
        data: {
            authorities: ['ROLE_RESEARCHER', 'ROLE_ORGANISATION_COORDINATOR', 'ROLE_REVIEWER'],
            breadcrumb: 'requests'
        },
        canActivate: [ UserRouteAccessService ]
}];
