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
import { UserRouteAccessService } from '../../core/auth/user-route-access.service';
import { RequestMainDetailComponent } from './request-main-detail.component';
import { RequestDetailResolver } from './request-main-detail-resolver.service';

export const requestMainDetailRoute: Route = {
    path: 'detail/:uuid',
    component: RequestMainDetailComponent,
    data: {
        authorities: ['ROLE_RESEARCHER', 'ROLE_ORGANISATION_COORDINATOR', 'ROLE_REVIEWER'],
        pageTitle: 'request.pageTitle',
        breadcrumb: 'details'
    },
    canActivate: [UserRouteAccessService],
    resolve: {
        request: RequestDetailResolver
    }
};
