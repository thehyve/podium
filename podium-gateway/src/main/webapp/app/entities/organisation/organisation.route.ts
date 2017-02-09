import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes, CanActivate } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PaginationUtil } from 'ng-jhipster';

import { OrganisationComponent } from './organisation.component';
import { OrganisationDetailComponent } from './organisation-detail.component';
import { OrganisationPopupComponent } from './organisation-dialog.component';
import { OrganisationDeletePopupComponent } from './organisation-delete-dialog.component';

import { Principal } from '../../shared';

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

export const organisationRoute: Routes = [
  {
    path: 'organisation',
    component: OrganisationComponent,
    resolve: {
      'pagingParams': OrganisationResolvePagingParams
    },
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'podiumGatewayApp.organisation.home.title'
    }
  }, {
    path: 'organisation/:uuid',
    component: OrganisationDetailComponent,
    data: {
        authorities: ['ROLE_USER'],
        pageTitle: 'podiumGatewayApp.organisation.home.title'
    }
  }
];

export const organisationPopupRoute: Routes = [
  {
    path: 'organisation-new',
    component: OrganisationPopupComponent,
    data: {
        authorities: ['ROLE_PODIUM_ADMIN'],
        pageTitle: 'podiumGatewayApp.organisation.home.title'
    },
    outlet: 'popup'
  },
  {
    path: 'organisation/:uuid/edit',
    component: OrganisationPopupComponent,
    data: {
        authorities: ['ROLE_PODIUM_ADMIN'],
        pageTitle: 'podiumGatewayApp.organisation.home.title'
    },
    outlet: 'popup'
  },
  {
    path: 'organisation/:uuid/delete',
    component: OrganisationDeletePopupComponent,
    data: {
        authorities: ['ROLE_PODIUM_ADMIN'],
        pageTitle: 'podiumGatewayApp.organisation.home.title'
    },
    outlet: 'popup'
  }
];
