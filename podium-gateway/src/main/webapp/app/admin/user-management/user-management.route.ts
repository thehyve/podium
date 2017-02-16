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

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { UserMgmtComponent } from './user-management.component';
import { UserMgmtDetailComponent } from './user-management-detail.component';
import { UserDialogComponent } from './user-management-dialog.component';
import { UserDeleteDialogComponent } from './user-management-delete-dialog.component';

import { Principal } from '../../shared';


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

export const userMgmtRoute: Routes = [
  {
    path: 'user-management',
    component: UserMgmtComponent,
    resolve: {
      'pagingParams': UserResolvePagingParams
    },
    data: {
      pageTitle: 'userManagement.home.title'
    }
  },
  {
    path: 'user-management/:login',
    component: UserMgmtDetailComponent,
    data: {
      pageTitle: 'userManagement.home.title'
    }
  }
];

export const userDialogRoute: Routes = [
  {
    path: 'user-management-new',
    component: UserDialogComponent,
    outlet: 'popup'
  },
  {
    path: 'user-management/:login/edit',
    component: UserDialogComponent,
    outlet: 'popup'
  },
  {
    path: 'user-management/:login/delete',
    component: UserDeleteDialogComponent,
    outlet: 'popup'
  }
];
