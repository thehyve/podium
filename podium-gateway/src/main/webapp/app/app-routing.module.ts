/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { homeRoute } from './home/home.route';
import { dashboardRoute } from './dashboard/dashboard.route';
import { navbarRoute } from './layouts/navbar/navbar.route';
import { errorRoute } from './layouts/error/error.route';
import { completedRoute } from './shared/completed/completed.route';

let LAYOUT_ROUTES = [
    homeRoute,
    dashboardRoute,
    navbarRoute,
    ...completedRoute
];

@NgModule({
    imports: [
        RouterModule.forRoot(LAYOUT_ROUTES, {useHash: true})
    ],
    exports: [
        RouterModule
    ]
})
export class AppRoutingModule {}

@NgModule({
    imports: [
        RouterModule.forRoot(errorRoute, {useHash: true})
    ],
    exports: [RouterModule]
})
export class AppErrorRoutingModule {}
