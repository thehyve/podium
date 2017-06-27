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
import { ITEMS_PER_PAGE } from '../constants/pagination.constants';
import { RequestOverviewPath } from '../../request/overview/request-overview.constants';

/**
 * A class to govern all functionality for overview components
 */
export abstract class Overview {

    protected currentSearch: any;
    protected queryCount: any;
    protected itemsPerPage: any = ITEMS_PER_PAGE;
    protected page: any;
    protected pageHeader: string;
    protected predicate: any;
    protected previousPage: any;
    protected reverse: any = false;
    protected totalItems: any;
    protected links: any;
    protected routePath: any;

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
    protected sort() {
        return [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
    }

    /**
     * Compose an object holding the current page parameters to be used in REST calls.
     *
     * @returns {any} composed object holding the parameters.
     */
    protected getPageParams(): any {
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

    protected transition() {
        // Transition with queryParams
        // Update the URL with the new parameters
        let params = this.getPageParams();

        this.router.navigate([this.getNavUrlForRouter(this.router)], {
            queryParams: {
                page: this.page,
                size: this.itemsPerPage,
                search: this.currentSearch,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            }
        });
    }

    protected loadPage(page: number, callback: Function) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            callback();
        }
    }

    protected resetPagingParams() {
        this.page = 1;
        this.predicate = 'createdDate';
        this.reverse = false;
        this.previousPage = null;
        this.itemsPerPage = ITEMS_PER_PAGE;
    }

    protected search (query, callback) {
        if (!query) {
            return this.clear(callback);
        }
        this.page = 0;
        this.currentSearch = query;

        // Transition with matrix params
        this.router.navigate([this.getNavUrlForRouter(this.router),
            {
                search: this.currentSearch,
                page: this.page,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            }]);

        // reload resource
        callback();
    }

    protected clear(callback: Function) {
        this.page = 0;
        this.currentSearch = '';
        this.router.navigate([this.getNavUrlForRouter(this.router), {
            page: this.page,
            sort: [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')]
        }]);
        callback();
    }

    protected isResearcherRoute(): boolean {
        return this.routePath === RequestOverviewPath.REQUEST_OVERVIEW_RESEARCHER;
    }

    protected isCoordinatorRoute(): boolean {
        return this.routePath === RequestOverviewPath.REQUEST_OVERVIEW_COORDINATOR;
    }

    protected isReviewerRoute(): boolean {
        return this.routePath === RequestOverviewPath.REQUEST_OVERVIEW_REVIEWER;
    }

    protected getNavUrlForRouter(router: Router) {
        return this.router.url.split(/\?/)[0].split(/;/)[0] + '/';
    }
}
