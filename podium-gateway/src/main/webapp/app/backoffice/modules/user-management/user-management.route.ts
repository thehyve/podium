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
import { parseAscending, parsePage, parsePredicate } from '../../../shared/util/pagination-util';
import { UserMgmtComponent } from './user-management.component';
import { UserDialogComponent } from './user-management-dialog.component';
import { UserDeleteDialogComponent } from './user-management-delete-dialog.component';
import { UserUnlockDialogComponent } from './user-management-unlock-dialog.component';
import { AccountService } from '../../../core/auth/account.service';
import { UserRouteAccessService } from '../../../core/auth/user-route-access.service';


@Injectable()
export class UserResolve implements CanActivate {

  constructor(private accountService: AccountService) { }

  canActivate() {
    return this.accountService.identity().then(() => this.accountService.hasAnyAuthority(['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN']));
  }
}

@Injectable()
export class UserResolvePagingParams implements Resolve<any> {

  constructor() {}

  resolve(route: ActivatedRouteSnapshot) {
      let page = route.queryParams['page'] ? route.queryParams['page'] : '1';
      let sort = route.queryParams['sort'] ? route.queryParams['sort'] : 'createdDate,desc';
      return {
          page: parsePage(page),
          predicate: parsePredicate(sort),
          ascending: parseAscending(sort)
    };
  }
}

export const userDialogRoute: Routes = [
    {
        path: 'new',
        component: UserDialogComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
            breadcrumb: 'new user'
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
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_ORGANISATION_ADMIN'],
            pageTitle: 'userManagement.home.title',
            breadcrumb: 'user management overview'
        },
        canActivate: [UserRouteAccessService]
    },
    ...userDialogRoute

];

