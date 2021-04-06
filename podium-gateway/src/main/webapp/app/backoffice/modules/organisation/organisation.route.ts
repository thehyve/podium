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
import { UserRouteAccessService } from '../../../core/auth/user-route-access-service';
import { JhiPaginationUtil } from 'ng-jhipster';
import { OrganisationComponent } from './organisation.component';
import { OrganisationDeletePopupComponent } from './organisation-delete-dialog.component';
import { OrganisationFormComponent } from './organisation-form/organisation-form.component';

@Injectable()
export class OrganisationResolvePagingParams implements Resolve<any> {

    constructor(private paginationUtil: JhiPaginationUtil) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        let page = route.queryParams['page'] ? route.queryParams['page'] : '1';
        let sort = route.queryParams['sort'] ? route.queryParams['sort'] : 'shortName,asc';

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
            pageTitle: 'organisation.home.title',
            breadcrumb: 'overview'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'new',
        component: OrganisationFormComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
            pageTitle: 'organisation.detail.title',
            breadcrumb: 'new organisation'
        },
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'detail/:uuid/delete',
        component: OrganisationDeletePopupComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN'],
            pageTitle: 'organisation.home.title'
        },
        outlet: 'popup',
        canActivate: [UserRouteAccessService]
    },
    {
        path: 'edit/:uuid',
        component: OrganisationFormComponent,
        data: {
            authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_ORGANISATION_ADMIN'],
            pageTitle: 'organisation.detail.title',
            breadcrumb: 'edit organisation'
        },
        canActivate: [UserRouteAccessService]
    }
];

