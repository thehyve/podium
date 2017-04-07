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
import { UserRouteAccessService } from '../../shared/auth/user-route-access-service';
import { RequestOverviewComponent } from './request-overview.component';

export const requestOverviewRoute: Route = {
    path: 'overview',
    component: RequestOverviewComponent,
    data: {
        authorities: ['ROLE_RESEARCHER'],
        pageTitle: 'request.pageTitle'
    },
    canActivate: [UserRouteAccessService]
};
