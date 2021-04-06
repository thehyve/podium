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
import { UserRouteAccessService } from '../../core/auth/user-route-access-service';
import { RequestFormComponent } from './request-form.component';

export const requestFormEditRoute: Route = {
    path: 'edit/:uuid',
    component: RequestFormComponent,
    data: {
        authorities: ['ROLE_RESEARCHER'],
        pageTitle: 'request.pageTitle',
        breadcrumb: 'edit'
    },
    canActivate: [UserRouteAccessService]
};
