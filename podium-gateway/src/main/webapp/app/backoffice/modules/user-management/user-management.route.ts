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
import { PaginationUtil } from 'ng-jhipster';
import { UserMgmtComponent } from './user-management.component';
import { UserMgmtDetailComponent } from './user-management-detail.component';
import { UserDialogComponent } from './user-management-dialog.component';
import { UserDeleteDialogComponent } from './user-management-delete-dialog.component';
import { UserUnlockDialogComponent } from './user-management-unlock-dialog.component';
import { Principal } from '../../../shared/auth/principal.service';
import { UserRouteAccessService } from '../../../shared/auth/user-route-access-service';


@Injectable()
export class UserResolve implements CanActivate {

  constructor(private principal: Principal) { }

  canActivate() {
    return this.principal.identity().then(account => this.principal.hasAnyAuthority(['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN']));
  }
}

@Injectable()
export class UserResolvePagingParams implements Resolve<any> {

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

export const userDialogRoute: Routes = [
    {
        path: 'new',
        component: UserDialogComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'detail/:login/edit',
        component: UserDialogComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'detail/:login/delete',
        component: UserDeleteDialogComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'detail/:login/unlock',
        component: UserUnlockDialogComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    }
];

export const userMgmtRoute: Routes = [
    {
        path: '',
        component: UserMgmtComponent,
        resolve: {
            pagingParams: UserResolvePagingParams
        },
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
            pageTitle: 'userManagement.home.title'
        },
        canActivate: [UserRouteAccessService],
    },
    {
        path: 'detail/:login',
        component: UserMgmtDetailComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
            pageTitle: 'userManagement.home.title'
        },
        canActivate: [UserRouteAccessService]
    },
    ...userDialogRoute

];

