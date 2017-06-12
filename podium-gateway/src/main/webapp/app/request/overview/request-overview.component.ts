/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, OnDestroy, ViewChild, InjectionToken } from '@angular/core';
import { OverviewServiceConfig } from '../../shared/overview/overview.service.config';
import { OverviewService } from '../../shared/overview/overview.service';
import { JhiLanguageService, EventManager, ParseLinks } from 'ng-jhipster';
import { RequestBase } from '../../shared/request/request-base';
import { RequestService } from '../../shared/request/request.service';
import { Router, ActivatedRoute } from '@angular/router';
import { Principal } from '../../shared';
import { RequestFormService } from '../form/request-form.service';
import { Subscription } from 'rxjs';
import { RequestStatusOptions } from '../../shared/request/request-status/request-status.constants';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { RequestDraftDeleteModalComponent } from './delete-request-draft-modal.component';
import { RequestOverviewPath } from './request-overview.constants';
import { Response, Http } from '@angular/http';
import { Overview } from '../../shared/overview/overview';
import { RequestStatusSidebarComponent } from '../../shared/request/status-sidebar/status-sidebar.component';
import { UserGroupAuthority } from '../../shared/authority/authority.constants';

let overviewConfig: OverviewServiceConfig = {
    resourceUrl: 'api/requests',
    resourceSearchUrl: 'api/_search/requests'
};

/**
 * Request overview component.
 * Uses its own configured instance of the OverviewService
 */
@Component({
    selector: 'pdm-request-overview',
    templateUrl: './request-overview.component.html',
    providers: [
        {
            provide: OverviewService,
            useFactory: (http: Http) => {
                return new OverviewService(http, overviewConfig);
            },
            deps: [
                Http
            ]
        }
    ]
})
export class RequestOverviewComponent extends Overview implements OnInit, OnDestroy {

    @ViewChild(RequestStatusSidebarComponent)
    private requestSidebarComponent: RequestStatusSidebarComponent;

    availableRequests: RequestBase[];
    error: string;
    success: string;
    eventSubscriber: Subscription;
    requestsSubscription: Subscription;
    currentRequestStatus: RequestStatusOptions;
    routePath: any;
    toggledSidebar = true; // open by default
    userGroupAuthority: UserGroupAuthority;

    // FIXME: Major refactor of overview component.
    constructor(
        private jhiLanguageService: JhiLanguageService,
        private requestService: RequestService,
        private parseLinks: ParseLinks,
        private requestFormService: RequestFormService,
        private eventManager: EventManager,
        private modalService: NgbModal,
        private overviewService: OverviewService,
        protected router: Router,
        protected activatedRoute: ActivatedRoute
    ) {
        super(router, activatedRoute);

        this.jhiLanguageService.setLocations(['request']);
        this.routePath = this.activatedRoute.snapshot.url[0].path;

        this.requestsSubscription = this.overviewService.onOverviewUpdate.subscribe(
            (res: Response) => this.processAvailableRequests(res.json(), res.headers),
            (err): any => this.onError(err)
        );
    }

    ngOnInit(): void {
        this.currentRequestStatus = RequestStatusOptions.Review; // begin with submitted requests

        console.log('Route ', this.routePath);

        switch (this.routePath) {
            case RequestOverviewPath.REQUEST_OVERVIEW_RESEARCHER:
                console.log('Match 1');
                this.userGroupAuthority = UserGroupAuthority.Requester;
                break;
            case RequestOverviewPath.REQUEST_OVERVIEW_COORDINATOR:
                console.log('Match 2');
                this.userGroupAuthority = UserGroupAuthority.Coordinator;
                break;
            case RequestOverviewPath.REQUEST_OVERVIEW_REVIEWER:
                console.log('Match 3');
                this.userGroupAuthority = UserGroupAuthority.Reviewer;
                break;
            default:
                console.log('No match ', this.routePath);
        }

        // this.requestSidebarComponent.userGroupAuthority =
        this.registerChangeInRequests();
    }

    /**
     * Subscription clean up to prevent memory leaks
     */
    ngOnDestroy() {
        if (this.requestsSubscription) {
            this.requestsSubscription.unsubscribe();
        }

        if (this.eventSubscriber) {
            this.eventManager.destroy(this.eventSubscriber);
        }
    }

    registerChangeInRequests() {
        this.eventSubscriber = this.eventManager.subscribe('requestListModification', (response) => this.loadRequests());
    }

    loadRequests() {
        console.log('this.cur ', this.currentRequestStatus);
        if (this.currentRequestStatus === RequestStatusOptions.Draft) {
            this.loadDrafts();
        } else if (this.currentRequestStatus === RequestStatusOptions.Review) {
            if (this.routePath === RequestOverviewPath.REQUEST_OVERVIEW_COORDINATOR) {
                this.loadCoordinatorReviewRequests();
            } else if (this.routePath === RequestOverviewPath.REQUEST_OVERVIEW_REVIEWER) {
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
                (error) => this.onError('Error loading available coordinator review request.')
            );
    }

    loadCoordinatorDeliveryRequests() {
        this.currentRequestStatus = RequestStatusOptions.Delivery;
        this.requestService.findCoordinatorDeliveryRequests(this.getPageParams())
            .subscribe(
                (res) => this.processAvailableRequests(res.json(), res.headers),
                (error) => this.onError('Error loading available coordinator delivery request .')
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

    loadMyDeliveryRequests(): void {
        this.currentRequestStatus = RequestStatusOptions.Delivery;
        this.requestService.findMyDeliveryRequests(this.getPageParams())
            .subscribe(
                (res) => this.processAvailableRequests(res.json(), res.headers),
                (error) => this.onError('Error loading available delivery requests.')
            );
    }

    editRequest(request) {
        this.requestFormService.request = request;
        this.router.navigate(['./requests/edit']);
    }

    deleteDraft(request) {
        const modalRef = this.modalService.open(RequestDraftDeleteModalComponent);
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

    loadPage(page: number, callback: Function) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            callback();
        }
    }

    toggleSidebar() {
        this.toggledSidebar = !this.toggledSidebar;
    }

    transitionRequests() {
        return this.transition(this.loadRequests.bind(this));
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
