/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Route, ActivatedRouteSnapshot, Resolve } from '@angular/router';
import { UserRouteAccessService } from '../../core/auth/user-route-access.service';
import { RequestOverviewComponent } from './request-overview.component';
import { parseAscending, parsePage, parsePredicate } from '../../shared/util/pagination-util';
import { Injectable } from '@angular/core';
import { RequestOverviewPath } from './request-overview.constants';

@Injectable()
export class RequestResolvePagingParams implements Resolve<any> {

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

export const requestOverviewRoute: Route = {
    path: RequestOverviewPath.REQUEST_OVERVIEW_RESEARCHER,
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
    path: RequestOverviewPath.REQUEST_OVERVIEW_COORDINATOR,
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
    path: RequestOverviewPath.REQUEST_OVERVIEW_REVIEWER,
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
