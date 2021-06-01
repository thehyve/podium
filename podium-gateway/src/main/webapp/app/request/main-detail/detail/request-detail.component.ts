/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { RequestDetail } from '../../../shared/request/request-detail';
import { RequestBase } from '../../../shared/request/request-base';
import { RequestService } from '../../../shared/request/request.service';
import { RequestAccessService } from '../../../shared/request/request-access.service';
import { RequestOverviewStatusOption } from '../../../shared/request/request-status/request-status.constants';
import { RequestFormService } from '../../form/request-form.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { RequestReviewDecision } from '../../../shared/request/request-review-decision';
import { RequestUpdateReviewDialogComponent } from '../../../shared/status-update/request-update-review-dialog.component';
import { RequestStatusUpdateAction } from '../../../shared/status-update/request-update-action';
import { RequestUpdateStatusDialogComponent } from '../../../shared/status-update/request-update-status-dialog.component';
import { AccountService } from '../../../core/auth/account.service';
import { User } from '../../../shared/user/user.model';
import { RequestFinalizeDialogComponent } from '../request-finalize-dialog/request-finalize-dialog.component';
import { Delivery } from '../../../shared/delivery/delivery';
import { Subscription } from 'rxjs';
import { DeliveryService } from '../../../shared/delivery/delivery.service';
import { AlertService } from '../../../core/util/alert.service';

@Component({
    selector: 'pdm-request-detail',
    templateUrl: './request-detail.component.html'
})

export class RequestDetailComponent implements OnInit, OnDestroy {

    public RequestReviewDecision: typeof RequestReviewDecision = RequestReviewDecision;

    public request: RequestBase;
    public requestDetails: RequestDetail;
    public deliveries: Delivery[];
    public isInRevision = false;
    public isUpdating = false;
    public currentUser: User;

    public authenticationSubscription: Subscription;
    public requestSubscription: Subscription;
    public deliveriesSubscription: Subscription;

    constructor(
        private requestService: RequestService,
        private deliveryService: DeliveryService,
        private requestAccessService: RequestAccessService,
        private requestFormService: RequestFormService,
        private modalService: NgbModal,
        private accountService: AccountService,
        private alertService: AlertService
    ) {
    }

    ngOnInit() {
        this.registerChanges();
    }

    /**
     * Subscription clean up to prevent memory leaks
     */
    ngOnDestroy() {
        if (this.requestSubscription) {
            this.requestSubscription.unsubscribe();
        }

        if (this.deliveriesSubscription) {
            this.deliveriesSubscription.unsubscribe();
        }

        if (this.authenticationSubscription) {
            this.authenticationSubscription.unsubscribe();
        }
    }

    /**
     * Setup change detection
     */
    registerChanges() {
        this.requestSubscription = this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.setRequest(request);
        });

        this.deliveriesSubscription = this.deliveryService.onDeliveries.subscribe(
            (deliveries: Delivery[]) => {
                this.deliveries = deliveries;
            }
        );

        this.authenticationSubscription = this.accountService.getAuthenticationState().subscribe(
            (identity: User) => {
                this.currentUser = identity;
                this.checkIsInRevision(this.request);
            },
            (err) => this.onError(err)
        );
    }

    /**
     * Set the request so we can perform a check whether the request is in Revision.
     *
     * @param request the request
     */
    setRequest(request) {
        this.request = request;

        if (request) {
            this.requestDetails = request.requestDetail;
            this.isInRevision = false;

            this.checkIsInRevision(request);
        }
    }

    /**
     * Check whether the request is in Revision.
     *
     * @param request the request
     */
    private checkIsInRevision(request) {
        if (!request) {
            return;
        }
        if (this.isRevisionStatusForRequester(request)) {
            this.isInRevision = true;
            this.requestFormService.request = request;
        }
    }

    /**
     * Submit the feedback of a reviewer for a request.
     *
     * @param decision the review feedback holding the advice and their findings.
     */
    submitReview(decision: RequestReviewDecision) {
        let modalRef = this.modalService.open(RequestUpdateReviewDialogComponent, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.request = this.request;
        modalRef.componentInstance.currentUser = this.currentUser;
        modalRef.componentInstance.reviewStatus = decision;
        modalRef.result.then(() => {
            this.requestService.requestUpdateEvent(this.request);
            this.isUpdating = false;
        }, (reason) => {
            this.onError(reason);
            this.isUpdating = false;
        });
    }

    /**
     * Send a request back for revision.
     * A confirmation dialog is opened so the Organisation Coordinator can supply their argumentation.
     */
    requireRequestRevision() {
        this.isUpdating = true;
        return this.confirmStatusUpdateModal(this.request, RequestStatusUpdateAction.Revision);
    }

    /**
     * Validate a request and send on for review
     */
    validateRequest() {
        this.isUpdating = true;
        this.requestService.validateRequest(this.request.uuid)
            .subscribe(
                (res) => {
                    this.onSuccess(res);
                    this.registerChanges();
                },
                (err) => this.onError(err)
            );
    }

    /**
     * Check whether the current logged in user is a organisation coordinator for the request.
     * @returns {boolean} true if the current user is a organisation coordinator for the request.
     */
    isRequestCoordinator(): boolean {
        return this.requestAccessService.isCoordinatorFor(this.request);
    }

    isRequestReviewer(): boolean {
        return this.requestAccessService.isReviewerFor(this.request);
    }

    isRequestingResearcher(): boolean {
        return this.requestAccessService.isRequesterOf(this.request);
    }

    /**
     * Approve a request
     */
    approveRequest() {
        this.isUpdating = true;
        this.requestService.approveRequest(this.request.uuid)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    /**
     * Close a request.
     * A confirmation modal is shown for the organisation coordinator to provide the reason for closing the request.
     */
    closeRequest() {
        this.isUpdating = true;
        return this.confirmStatusUpdateModal(this.request, RequestStatusUpdateAction.Close);
    }

    /**
     * Reject a request.
     * A confirmation modal is shown for the organisation coordinator to provide their findings.
     */
    rejectRequest() {
        this.isUpdating = true;
        return this.confirmStatusUpdateModal(this.request, RequestStatusUpdateAction.Reject);
    }

    /**
     * Start the delivery process of a request using its UUID.
     */
    startRequestDelivery() {
        this.isUpdating = true;
        this.requestService.startRequestDelivery(this.request.uuid)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    /**
     * Finalize the request process
     */
    finalizeRequest() {
        return this.confirmFinalizeRequest(this.request, this.deliveries);
    }

    /**
     * Open a modal window for providing feedback details for Rejecting or requesting a revision.
     *
     * @param request the request
     * @param action the specific RequestStatusUpdateAction to apply to the request.
     */
    confirmStatusUpdateModal(request: RequestBase, action: RequestStatusUpdateAction) {
        let modalRef = this.modalService.open(RequestUpdateStatusDialogComponent, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.request = request;
        modalRef.componentInstance.statusUpdateAction = action;
        modalRef.result.then(() => {
            this.isUpdating = false;
        }, () => {
            this.isUpdating = false;
        });
    }

    /**
     * Open a modal window giving the organisation coordinator a summary of the outcome of the request.
     *
     * @param request the request
     * @param deliveries the deliveries belonging to the request.
     */
    confirmFinalizeRequest(request: RequestBase, deliveries: Delivery[]) {
        let modalRef = this.modalService.open(RequestFinalizeDialogComponent, {size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.request = request;
        modalRef.componentInstance.deliveries = deliveries;
        modalRef.result.then(() => {
            this.isUpdating = false;
        }, () => {
            this.isUpdating = false;
        });
    }

    /**
     * Check whether the request belongs to the current user and if the request is in revision
     *
     * @param request the request being processed
     * @returns {boolean} true if the user owns the request and it is in revision
     */
    isRevisionStatusForRequester(request: RequestBase): boolean {
        let revisionStatus = RequestAccessService.isRequestStatus(request, RequestOverviewStatusOption.Revision);
        let isRequester = this.requestAccessService.isRequesterOf(request);

        return revisionStatus && isRequester;
    }

    /**
     * Check whether the request has related requests
     * @returns true if the request has related requests
     */
    hasRelatedRequests(): boolean {
        return this.request.relatedRequests && this.request.relatedRequests.length > 0;
    }

    hasReviewRounds(): boolean {
        return this.request.reviewRound !== null;
    }

    /**
     * Indicates whether the logged in user is allowed to view the review panel
     * This is true when with the following conditions have been met; in order:
     *    - A review round is available
     *    - The user is the request coordinator or the request reviewer
     *    - When the request is not in the Validation phase (In this phase no review rounds will be active)
     * @returns {boolean} true if the user can view the review panel
     */
    showReviewPanel(): boolean {
        // Dont show if we dont have review rounds
        if (!this.hasReviewRounds()) {
            return false;
        }

        // Dont show if the user is not one of the request coordinators or reviewers
        if (!this.isRequestCoordinator() && !this.isRequestReviewer()) {
            return false;
        }

        /**
         * Show only if the request is not in review and in the validation phase
         * This is to cover the case when a request has been sent for revision
         * and the previous review round has been closed.
         */
        return this.request.status !== RequestOverviewStatusOption.Validation;
    }

    /**
     * Indicates whether the logged in user is allowed to view the delivery panel
     * @returns {boolean} true if the user can view the delivery panel
     */
    showDeliveryPanel(): boolean {
        return this.isRequestCoordinator() || this.isRequestingResearcher();
    }

    onSuccess(response: HttpResponse<RequestBase>) {
        this.request = response.body;
        this.isUpdating = false;
        this.requestService.requestUpdateEvent(this.request);
    }

    onError(error) {
        this.isUpdating = false;
        this.alertService.error(error.error, error.message, null);
    }
}
