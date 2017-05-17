/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Route, ActivatedRouteSnapshot, RouterStateSnapshot, Resolve } from '@angular/router';
import { UserRouteAccessService } from '../../shared/auth/user-route-access-service';
import { RequestOverviewComponent } from './request-overview.component';
import { PaginationUtil } from 'ng-jhipster';
import { Injectable } from '@angular/core';
import { requestOverviewPaths } from './request-overview.constants';

@Injectable()
export class RequestResolvePagingParams implements Resolve<any> {

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

export const requestOverviewRoute: Route = {
    path: requestOverviewPaths.REQUEST_OVERVIEW_RESEARCHER,
    component: RequestOverviewComponent,
    resolve: {
        'pagingParams': RequestResolvePagingParams
    },
    data: {
        authorities: ['ROLE_RESEARCHER'],
        pageTitle: 'request.pageTitle',
        pageHeader: 'request.overview.pageHeaderResearcher',
        breadcrumb: 'my requests'
    },
    canActivate: [UserRouteAccessService]
};

export const organisationRequestOverviewRoute: Route = {
    path: requestOverviewPaths.REQUEST_OVERVIEW_COORDINATOR,
    component: RequestOverviewComponent,
    resolve: {
        'pagingParams': RequestResolvePagingParams
    },
    data: {
        authorities: ['ROLE_ORGANISATION_COORDINATOR'],
        pageTitle: 'request.pageTitle',
        pageHeader: 'request.overview.pageHeaderCoordinator',
        breadcrumb: 'organisation requests',
    },
    canActivate: [UserRouteAccessService]
};

export const reviewerRequestOverviewRoute: Route = {
    path: requestOverviewPaths.REQUEST_OVERVIEW_REVIEWER,
    component: RequestOverviewComponent,
    resolve: {
        'pagingParams': RequestResolvePagingParams
    },
    data: {
        authorities: ['ROLE_REVIEWER'],
        pageTitle: 'request.pageTitle',
        pageHeader: 'request.overview.pageHeaderReviewer',
        breadcrumb: 'my reviews',
    },
    canActivate: [UserRouteAccessService]
};
