/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { ActivatedRoute, Router } from '@angular/router';
import { ITEMS_PER_PAGE } from '../../config/pagination.constants';
import { RequestOverviewPath } from '../../request/overview/request-overview.constants';
import { RouterHelper } from '../util/router-helper';

/**
 * A class to govern all functionality for overview components
 */
export abstract class Overview {

    currentSearch: any;
    queryCount: any;
    itemsPerPage: any = ITEMS_PER_PAGE;
    page: any;
    pageHeader: string;
    predicate: any;
    previousPage: any;
    reverse: any = false;
    totalItems: any;
    links: any;
    routePath: any;

    constructor(
        protected router: Router,
        protected activatedRoute: ActivatedRoute
    ) {
        if (this.activatedRoute.snapshot.url.length) {
            this.routePath = this.activatedRoute.snapshot.url[0].path;
        }

        this.activatedRoute.data.subscribe(data => {
            this.pageHeader = data['pageHeader'];
            this.page = data['pagingParams'].page;
            this.previousPage = data['pagingParams'].page;
            this.reverse = data['pagingParams'].ascending;
            this.predicate = data['pagingParams'].predicate;
        });

        this.currentSearch = activatedRoute.snapshot.params['search'] ? activatedRoute.snapshot.params['search'] : '';
    }

    /**
     * Compose the sorting object containing the predicate and the sorting order.
     * The id field is excluded from sorting.
     *
     * @returns {string[]}
     */
    sort() {
        return [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
    }

    /**
     * Compose an object holding the current page parameters to be used in REST calls.
     *
     * @returns {any} composed object holding the parameters.
     */
    getPageParams(): any {
        let params: any = {
            size: this.itemsPerPage,
            sort: this.sort()
        };

        if (this.currentSearch) {
            params.query = this.currentSearch;
        }

        params.page = this.page > 0 ? this.page - 1 : 0;

        return params;
    };

    transition() {
        // Transition with queryParams
        // Update the URL with the new parameters
        this.router.navigate([RouterHelper.getNavUrlForRouter(this.router)], {
            queryParams: {
                page: this.page,
                size: this.itemsPerPage,
                search: this.currentSearch,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            }
        });
    }

    loadPage(page: number, callback: () => any) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            callback();
        }
    }

    resetPagingParams() {
        this.page = 1;
        this.predicate = 'createdDate';
        this.reverse = false;
        this.previousPage = null;
        this.itemsPerPage = ITEMS_PER_PAGE;
    }

    search (query, callback) {
        if (!query) {
            return this.clear(callback);
        }
        this.page = 0;
        this.currentSearch = query;

        // Transition with matrix params
        this.router.navigate([RouterHelper.getNavUrlForRouter(this.router),
            {
                search: this.currentSearch,
                page: this.page,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            }]);

        // reload resource
        callback();
    }

    clear(callback: () => any) {
        this.page = 0;
        this.currentSearch = '';
        this.router.navigate([RouterHelper.getNavUrlForRouter(this.router), {
            page: this.page,
            sort: [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')]
        }]);
        callback();
    }

    isResearcherRoute(): boolean {
        return this.routePath === RequestOverviewPath.REQUEST_OVERVIEW_RESEARCHER;
    }

    isCoordinatorRoute(): boolean {
        return this.routePath === RequestOverviewPath.REQUEST_OVERVIEW_COORDINATOR;
    }

    isReviewerRoute(): boolean {
        return this.routePath === RequestOverviewPath.REQUEST_OVERVIEW_REVIEWER;
    }
}
