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

export const dashboardRoute: Route = {
    path: 'dashboard',
    component: DashboardComponent,
    data: {
        authorities: [],
        pageTitle: 'dashboard.title',
        breadcrumb: 'dashboard'
    },
    canActivate: [UserRouteAccessService]
};
