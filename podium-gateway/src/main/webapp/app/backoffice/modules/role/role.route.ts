/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { RoleComponent } from './role.component';
import { RoleDetailComponent } from './role-detail.component';
import { RolePopupComponent } from './role-dialog.component';

@Injectable()
export class RoleResolvePagingParams implements Resolve<any> {

  constructor(private paginationUtil: PaginationUtil) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
      let page = route.queryParams['page'] ? route.queryParams['page'] : '1';
      let sort = route.queryParams['sort'] ? route.queryParams['sort'] : 'id,asc';
      return {
          page: this.paginationUtil.parsePage(page),
          predicate: this.paginationUtil.parsePredicate(sort),
          ascending: this.paginationUtil.parseAscending(sort)
    };
  }
}

export const roleRoute: Routes = [
  {
    path: 'backoffice/role',
    component: RoleComponent,
    resolve: {
      'pagingParams': RoleResolvePagingParams
    },
    data: {
        authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_ORGANISATION_ADMIN'],
        pageTitle: 'podiumGatewayApp.role.home.title'
    },
    canActivate: [UserRouteAccessService]
  }, {
    path: 'backoffice/role/:id',
    component: RoleDetailComponent,
    data: {
        authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_ORGANISATION_ADMIN'],
        pageTitle: 'podiumGatewayApp.role.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const rolePopupRoute: Routes = [
  {
    path: 'backoffice/role/:id/edit',
    component: RolePopupComponent,
    data: {
        authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_ORGANISATION_ADMIN'],
        pageTitle: 'podiumGatewayApp.role.home.title'
    },
    outlet: 'popup',
    canActivate: [UserRouteAccessService]
  },
];
