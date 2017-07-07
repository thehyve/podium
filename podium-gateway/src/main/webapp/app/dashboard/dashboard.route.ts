/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Route } from '@angular/router';
import { UserRouteAccessService } from '../shared';
import { DashboardComponent } from './';
import { PodiumAuthorityOptions } from '../shared/authority/authority.constants';

export const dashboardRoute: Route = {
    path: 'dashboard',
    component: DashboardComponent,
    data: {
        authorities: [
            PodiumAuthorityOptions.ROLE_PODIUM_ADMIN,
            PodiumAuthorityOptions.ROLE_BBMRI_ADMIN,
            PodiumAuthorityOptions.ROLE_RESEARCHER
        ],
        pageTitle: 'dashboard.title',
        breadcrumb: 'dashboard'
    },
    canActivate: [UserRouteAccessService]
};
