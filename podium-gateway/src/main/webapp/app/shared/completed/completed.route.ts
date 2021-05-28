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
import { UserRouteAccessService } from '../../core/auth/user-route-access.service';
import { CompletedComponent } from './completed.component';

export const completedRoute: Routes = [
    {
        path: 'completed',
        component: CompletedComponent,
        data: {
            authorities: [],
            pageTitle: 'completed.title'
        },
        canActivate: [UserRouteAccessService]
    },
];
