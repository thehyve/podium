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
import { NotFoundComponent } from './not-found.component';
import { UserRouteAccessService } from '../auth/user-route-access-service';

export const notFoundRoute: Routes = [
    {
        path: '404',
        component: NotFoundComponent,
        data: {
            authorities: [],
            pageTitle: 'error.title'
        },
        canActivate: [UserRouteAccessService]
    }
];
