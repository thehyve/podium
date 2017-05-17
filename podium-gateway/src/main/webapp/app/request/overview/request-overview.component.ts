/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { JhiLanguageService, EventManager, ParseLinks } from 'ng-jhipster';
import { RequestBase } from '../../shared/request/request-base';
import { RequestService } from '../../shared/request/request.service';
import { User } from '../../shared/user/user.model';
import { Router, ActivatedRoute } from '@angular/router';
import { Principal } from '../../shared';
import { RequestFormService } from '../form/request-form.service';
import { Subscription } from 'rxjs';
import { RequestStatusOptions } from '../../shared/request/request-status/request-status.constants';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { RequestDraftModalModalComponent } from './delete-request-draft-modal.component';
import { ITEMS_PER_PAGE } from '../../shared/constants/pagination.constants';
import { requestOverviewPaths } from './request-overview.constants';

@Component({
    selector: 'pdm-request-overview',
    templateUrl: './request-overview.component.html',
    styleUrls: ['request-overview.scss']
})

export class RequestOverviewComponent implements OnInit, OnDestroy {

    private currentUser: User;

    availableRequests: RequestBase[];
    error: string;
    success: string;
    eventSubscriber: Subscription;
    currentRequestStatus: RequestStatusOptions;
    totalItems: any;
    routeData: any;
    routePath: any;
    currentSearch: any;
    queryCount: any;
    itemsPerPage: any;
    page: any;
    pageHeader: string;
    predicate: any;
    previousPage: any;
    reverse: any;
    links: any;

    constructor(private jhiLanguageService: JhiLanguageService,
                private requestService: RequestService,
                private router: Router,
                private parseLinks: ParseLinks,
                private requestFormService: RequestFormService,
                private eventManager: EventManager,
                private principal: Principal,
                private modalService: NgbModal,
                private activatedRoute: ActivatedRoute
    ) {
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.routeData = this.activatedRoute.data.subscribe(data => {
            this.pageHeader = data['pageHeader'];
            this.page = data['pagingParams'].page;
            this.previousPage = data['pagingParams'].page;
            this.reverse = data['pagingParams'].ascending;
            this.predicate = data['pagingParams'].predicate;
        });
        this.currentSearch = activatedRoute.snapshot.params['search'] ? activatedRoute.snapshot.params['search'] : '';
        this.jhiLanguageService.setLocations(['request']);
        this.routePath = this.activatedRoute.snapshot.url[0].path;
    }

    getPageParams(): any {
        let params: any = {
            size: this.itemsPerPage,
            sort: this.sort()
        };
        if (this.currentSearch) {
            params.query = this.currentSearch;
        } else {
            params.page = this.page - 1;
        }
        return params;
    };

    isResearcherRoute(): boolean {
        return this.routePath === requestOverviewPaths.REQUEST_OVERVIEW_RESEARCHER;
    }

    ngOnInit(): void {
        this.currentRequestStatus = RequestStatusOptions.Review; // begin with submitted requests
        this.principal.identity().then((account) => {
            this.currentUser = account;
            this.loadRequests();
        });
        this.registerChangeInRequests();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInRequests() {
        this.eventSubscriber = this.eventManager.subscribe('requestListModification', (response) => this.loadRequests());
    }

    loadRequests() {
        if (this.currentRequestStatus === RequestStatusOptions.Draft) {
            this.loadDrafts();
        } else if (this.currentRequestStatus === RequestStatusOptions.Review) {
            if (this.routePath === requestOverviewPaths.REQUEST_OVERVIEW_COORDINATOR) {
                this.loadCoordinatorReviewRequests();
            } else if (this.routePath === requestOverviewPaths.REQUEST_OVERVIEW_REVIEWER) {
                this.loadAllReviewerRequests();
            } else {
                this.loadMyReviewRequests();
            }
        }
    }

    createNewRequest() {
        this.requestFormService.request = null;
        this.router.navigate(['./requests/new']);
    }

    loadAllReviewerRequests() {
        this.currentRequestStatus = RequestStatusOptions.Review;
        this.requestService.findAllReviewerRequests(this.getPageParams())
            .subscribe(
                (res) => this.processAvailableRequests(res.json(), res.headers),
                (error) => this.onError('Error loading available request drafts.')
            );
    }

    loadCoordinatorReviewRequests() {
        this.currentRequestStatus = RequestStatusOptions.Review;
        this.requestService.findCoordinatorReviewRequests(this.getPageParams())
            .subscribe(
                (res) => this.processAvailableRequests(res.json(), res.headers),
                (error) => this.onError('Error loading available request drafts.')
            );
    }

    loadDrafts() {
        this.currentRequestStatus = RequestStatusOptions.Draft;
        this.requestService.findDrafts(this.getPageParams())
            .subscribe(
                (res) => this.processAvailableRequests(res.json(), res.headers),
                (error) => this.onError('Error loading available request drafts.')
            );
    }

    loadMyReviewRequests(): void {
        this.currentRequestStatus = RequestStatusOptions.Review;
        this.requestService.findMyReviewRequests(this.getPageParams())
            .subscribe(
                (res) => this.processAvailableRequests(res.json(), res.headers),
                (error) => this.onError('Error loading available submitted requests.')
            );
    }

    sort() {
        let result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    editRequest(request) {
        this.requestFormService.request = request;
        this.router.navigate(['./requests/edit']);
    }

    deleteDraft(request) {
        const modalRef = this.modalService.open(RequestDraftModalModalComponent);
        modalRef.componentInstance.request = request;
        modalRef.result.then((result) => {
            console.log(`Closed ${result}`);
        }, (reason) => {
            console.log(`Dismissed ${reason}`);
        });
    }

    navigateToRequestDetail(request) {
        this.router.navigate(['./requests/detail', request.uuid]);
    }

    processAvailableRequests(requests, headers) {
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = headers.get('x-total-count');
        this.queryCount = this.totalItems;
        this.availableRequests = requests;
    }

    loadPage(page: number) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        // Transition with queryParams
        this.router.navigate([this.getNavUrlForRouter(this.router)], {
            queryParams: {
                page: this.page,
                size: this.itemsPerPage,
                search: this.currentSearch,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            }
        });
        this.loadRequests();
    }

    private getNavUrlForRouter(router: Router) {
        return this.router.url.split(/\?/)[0];
    }

    private onSuccess(result) {
        this.error = null;
        this.success = 'SUCCESS';
        window.scrollTo(0, 0);
    }

    private onError(error) {
        this.error = 'ERROR';
        this.success = null;
        window.scrollTo(0, 0);
    }
}
