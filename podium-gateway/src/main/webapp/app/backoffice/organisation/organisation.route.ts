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
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { OrganisationComponent } from './organisation.component';
import { OrganisationDetailComponent } from './organisation-detail.component';
import { OrganisationPopupComponent } from './organisation-dialog.component';
import { OrganisationDeletePopupComponent } from './organisation-delete-dialog.component';

@Injectable()
export class OrganisationResolvePagingParams implements Resolve<any> {

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

export const organisationPopupRoute: Routes = [
    {
        path: 'new',
        component: OrganisationPopupComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN'],
            pageTitle: 'podiumGatewayApp.organisation.home.title'
        },
        outlet: 'popup',
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'organisation/:uuid/edit',
        component: OrganisationPopupComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN'],
            pageTitle: 'podiumGatewayApp.organisation.home.title'
        },
        outlet: 'popup',
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'organisation/:uuid/delete',
        component: OrganisationDeletePopupComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN'],
            pageTitle: 'podiumGatewayApp.organisation.home.title'
        },
        outlet: 'popup',
        canActivate: [UserRouteAccessService]
    }
];

export const organisationRoute: Routes = [{
    path: '',
    children: [
      {
          path: '',
          component: OrganisationComponent,
          resolve: {
              'pagingParams': OrganisationResolvePagingParams
          },
          data: {
              authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
              pageTitle: 'podiumGatewayApp.organisation.home.title'
          },
          canActivate: [UserRouteAccessService]
      },
      {
          path: ':uuid',
          component: OrganisationDetailComponent,
          data: {
              authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
              pageTitle: 'podiumGatewayApp.organisation.home.title'
          },
          canActivate: [UserRouteAccessService]
      },
      ...organisationPopupRoute
    ]
}];

