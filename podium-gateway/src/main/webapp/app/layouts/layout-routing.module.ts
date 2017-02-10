/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes, Resolve } from '@angular/router';

import { homeRoute } from '../home';
import { navbarRoute } from '../app.route';
import { errorRoute } from './';
import {dashboardRoute} from '../dashboard/dashboard.route';

let LAYOUT_ROUTES = [
    homeRoute,
    dashboardRoute,
    navbarRoute,
    ...errorRoute
];

@NgModule({
  imports: [
    RouterModule.forRoot(LAYOUT_ROUTES, { useHash: true })
  ],
  exports: [
    RouterModule
  ]
})
export class LayoutRoutingModule {}
