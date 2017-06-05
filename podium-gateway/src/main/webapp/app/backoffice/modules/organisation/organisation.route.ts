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
import { UserRouteAccessService } from '../../../shared';
import { PaginationUtil } from 'ng-jhipster';
import { OrganisationComponent } from './organisation.component';
import { OrganisationDetailComponent } from './organisation-detail.component';
import { OrganisationDeletePopupComponent } from './organisation-delete-dialog.component';
import { OrganisationFormComponent } from './organisation-form/organisation-form.component';

@Injectable()
export class OrganisationResolvePagingParams implements Resolve<any> {

    constructor(private paginationUtil: PaginationUtil) {
    }

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
        path: '',
        component: OrganisationComponent,
        resolve: {
            'pagingParams': OrganisationResolvePagingParams
        },
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_ORGANISATION_ADMIN'],
            pageTitle: 'podiumGatewayApp.organisation.home.title',
            breadcrumb: 'overview'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'new',
        component: OrganisationFormComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
            pageTitle: 'podiumGatewayApp.organisation.detail.title',
            breadcrumb: 'new organisation'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'detail/:uuid/delete',
        component: OrganisationDeletePopupComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
            pageTitle: 'podiumGatewayApp.organisation.home.title'
        },
        outlet: 'popup',
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'detail/:uuid',
        component: OrganisationDetailComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_ORGANISATION_ADMIN'],
            pageTitle: 'podiumGatewayApp.organisation.home.title',
            breadcrumb: 'organisation details'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'edit/:uuid',
        component: OrganisationFormComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_ORGANISATION_ADMIN'],
            pageTitle: 'podiumGatewayApp.organisation.detail.title',
            breadcrumb: 'edit organisation'
        },
        canActivate: [UserRouteAccessService]
    }
];

