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
