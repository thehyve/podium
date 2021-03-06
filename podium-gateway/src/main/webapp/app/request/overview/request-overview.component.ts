/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, OnDestroy, ViewChild, AfterViewInit } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';

import { OverviewService } from '../../shared/overview/overview.service';
import { RequestBase } from '../../shared/request/request-base';
import { Router, ActivatedRoute } from '@angular/router';
import { RequestFormService } from '../form/request-form.service';
import { Subscription } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { RequestDraftDeleteModalComponent } from './delete-request-draft-modal.component';
import { RequestOverviewPath } from './request-overview.constants';
import { Overview } from '../../shared/overview/overview';
import { RequestStatusSidebarComponent } from '../../shared/request/status-sidebar/status-sidebar.component';
import { UserGroupAuthority } from '../../shared/authority/authority.constants';
import {
    RequestStatusSidebarOptions
} from '../../shared/request/status-sidebar/status-sidebar-options';
import { RequestType } from '../../shared/request/request-type';
import { parseLinks } from '../../shared/util/parse-links-util';
import { EventManager } from '../../core/util/event-manager.service';
import { RequestOverviewStatusOption } from '../../shared/request/request-status/request-status.constants';
import { ApplicationConfigService } from '../../core/config/application-config.service';

/**
 * Request overview component.
 * Uses its own configured instance of the OverviewService
 */
@Component({
    selector: 'pdm-request-overview',
    templateUrl: './request-overview.component.html',
    styleUrls: ['request-overview.scss'],
    providers: [
        {
            provide: OverviewService,
            useFactory: (
                config: ApplicationConfigService,
                http: HttpClient,
            ) => {
                let serviceConfig = {
                    getEndpoint(path: string) {
                        return config.getEndpointFor(`api/requests/${path}`);
                    },
                };
                return new OverviewService(serviceConfig, http);
            },
            deps: [
                ApplicationConfigService,
                HttpClient
            ]
        }
    ]
})
export class RequestOverviewComponent extends Overview implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild(RequestStatusSidebarComponent)
    private requestSidebarComponent: RequestStatusSidebarComponent;

    availableRequests: RequestBase[];
    error: string;
    success: string;
    eventSubscriber: Subscription;
    overviewSubscription: Subscription;
    sidebarSubscription: Subscription;
    activeStatus: RequestOverviewStatusOption;
    routePath: any;
    toggledSidebar = true; // open sidebar by default
    userGroupAuthority: UserGroupAuthority;
    statusSidebarOptions = RequestStatusSidebarOptions;

    constructor(
        private requestFormService: RequestFormService,
        private eventManager: EventManager,
        private modalService: NgbModal,
        private overviewService: OverviewService,
        protected router: Router,
        protected activatedRoute: ActivatedRoute
    ) {
        super(router, activatedRoute);

        this.activeStatus = this.overviewService.activeStatus || RequestOverviewStatusOption.All;

        this.overviewSubscription = this.overviewService.onOverviewUpdate.subscribe(
            (res: HttpResponse<RequestBase[]>) =>
                this.processAvailableRequests(res.body, res.headers),
            () => this.onError()
        );
    }

    ngOnInit(): void {
        switch (this.routePath) {
            case RequestOverviewPath.REQUEST_OVERVIEW_RESEARCHER:
                this.userGroupAuthority = UserGroupAuthority.Requester;
                break;
            case RequestOverviewPath.REQUEST_OVERVIEW_COORDINATOR:
                this.userGroupAuthority = UserGroupAuthority.Coordinator;
                break;
            case RequestOverviewPath.REQUEST_OVERVIEW_REVIEWER:
                this.userGroupAuthority = UserGroupAuthority.Reviewer;
                break;
            default:
                console.error('No user group authority', this.routePath);
        }
    }

    ngAfterViewInit() {
        this.registerChanges();

        this.fetchRequestsFor(this.activeStatus);
    }

    /**
     * Subscription clean up to prevent memory leaks
     */
    ngOnDestroy() {
        if (this.overviewSubscription) {
            this.overviewSubscription.unsubscribe();
        }

        if (this.sidebarSubscription) {
            this.sidebarSubscription.unsubscribe();
        }

        if (this.eventSubscriber) {
            this.eventManager.destroy(this.eventSubscriber);
        }
    }

    registerChanges() {
        this.eventSubscriber = this.eventManager
            .subscribe('requestListModification',
                () => this.fetchRequestsFor(this.activeStatus));


        this.sidebarSubscription = this.requestSidebarComponent.onStatusChange.subscribe(
            (newStatus) => this.fetchRequestsFor(newStatus)
        );
    }

    createNewRequest() {
        this.requestFormService.request = null;
        this.router.navigate(['./requests/new']);
    }

    editRequest(request) {
        this.requestFormService.request = request;
        this.router.navigate(['./requests/edit', request.uuid]);
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

    processAvailableRequests(requests: RequestBase[], headers) {
        this.links = parseLinks(headers.get('link'));
        this.totalItems = headers.get('x-total-count');
        this.queryCount = this.totalItems;
        this.availableRequests = requests;
    }

    transitionRequests() {
        this.transition();
        this.fetchRequestsFor(this.activeStatus);
    }

    fetchRequestsFor(option?: RequestOverviewStatusOption) {
        if (!option) {
            option = this.overviewService.activeStatus;
        }

        // Reset pagingParams when new status is selected
        if (option !== this.overviewService.activeStatus) {
            this.resetPagingParams();
        }

        this.overviewService
            .findRequestsForOverview(this.getPageParams(), option, this.userGroupAuthority)
            .subscribe((res) => {
                this.overviewService.overviewUpdateEvent(res);
                this.activeStatus = this.overviewService.activeStatus;
            });
    }

    toggleSidebar() {
        this.toggledSidebar = !this.toggledSidebar;
    }

    getIconForLinkedRequest(isLinked: boolean) {
        return isLinked ? 'link' : '';
    }

    getIconForRequestType(requestType: RequestType) {
        if (!requestType) {
            return null;
        }

        switch (requestType) {
            case RequestType.Data:
                return 'dns';
            case RequestType.Images:
                return 'image';
            case RequestType.Material:
                return 'blur_on';
            default:
                return '';
        }
    }

    isActiveStatus(activeStatus: typeof RequestStatusSidebarOptions): boolean {
        return this.activeStatus === activeStatus.option;
    }

    private onSuccess() {
        this.error = null;
        this.success = 'SUCCESS';
        window.scrollTo(0, 0);
    }

    private onError() {
        this.error = 'ERROR';
        this.success = null;
        window.scrollTo(0, 0);
    }

    getRequestId(_index, request: RequestBase) {
        return request.id;
    }

}
