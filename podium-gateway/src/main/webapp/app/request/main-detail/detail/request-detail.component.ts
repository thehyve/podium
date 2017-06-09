/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, ViewChild, OnDestroy } from '@angular/core';
import { RequestDetail } from '../../../shared/request/request-detail';
import { RequestBase } from '../../../shared/request/request-base';
import { RequestService } from '../../../shared/request/request.service';
import { RequestReviewFeedback } from '../../../shared/request/request-review-feedback';
import { RequestAccessService } from '../../../shared/request/request-access.service';
import { RequestReviewStatusOptions } from '../../../shared/request/request-status/request-status.constants';
import { RequestFormService } from '../../form/request-form.service';
import { Response } from '@angular/http';
import { Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { RequestStatusUpdateAction } from '../../../shared/status-update/request-status-update-action';
import { RequestStatusUpdateDialogComponent } from '../../../shared/status-update/request-status-update.component';
import { PodiumEventMessage } from '../../../shared/event/podium-event-message';
import { RequestFinalizeDialogComponent } from '../request-finalize-dialog/request-finalize-dialog.component';
import { Delivery } from '../../../shared/delivery/delivery';
import { Subscription } from 'rxjs';
import { DeliveryService } from '../../../shared/delivery/delivery.service';

@Component({
    selector: 'pdm-request-detail',
    templateUrl: './request-detail.component.html'
})

export class RequestDetailComponent implements OnDestroy {

    public request: RequestBase;
    public requestDetails: RequestDetail;
    public deliveries: Delivery[];
    public isInRevision = false;
    public isUpdating = false;

    public requestSubscription: Subscription;
    public deliveriesSubscription: Subscription;

    constructor(
        private requestService: RequestService,
        private deliveryService: DeliveryService,
        private requestAccessService: RequestAccessService,
        private requestFormService: RequestFormService,
        private modalService: NgbModal
    ) {
        // Forcefully reload logged in user
        this.requestAccessService.loadCurrentUser(true);

        this.requestSubscription = this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.setRequest(request);
        });

        this.deliveriesSubscription = this.deliveryService.onDeliveries.subscribe(
            (deliveries) => {
                this.deliveries = deliveries;
            }
        );
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

            if (this.isRevisionStatusForRequester(request)) {
                this.isInRevision = true;
                this.requestFormService.request = request;
            }
        }
    }

    /**
     * Submit the feedback of a reviewer for a request.
     *
     * @param requestReviewFeedback the reviewfeedback holding the advice and their findings.
     */
    submitReview(requestReviewFeedback: RequestReviewFeedback) {
        this.isUpdating = true;
        this.requestService.submitReview(this.request.uuid, requestReviewFeedback)
            .subscribe(
                (res) => this.onSuccess(res.json()),
                (err) => this.onError(err)
            );
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
                (res) => this.onSuccess(res),
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
        let modalRef = this.modalService.open(RequestStatusUpdateDialogComponent, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.request = request;
        modalRef.componentInstance.statusUpdateAction = action;
        modalRef.result.then(result => {
            console.log(`Closed with: ${result}`);
            this.isUpdating = false;
        }, (reason) => {
            console.log(`Dismissed ${reason}`);
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
        let modalRef = this.modalService.open(RequestFinalizeDialogComponent, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.request = request;
        modalRef.componentInstance.deliveries = deliveries;
        modalRef.result.then(result => {
            console.log(`Closed with: ${result}`);
            this.isUpdating = false;
        }, (reason) => {
            console.log(`Dismissed ${reason}`);
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
        let revisionStatus = this.requestAccessService.isRequestReviewStatus(request, RequestReviewStatusOptions.Revision);
        let isRequester = this.requestAccessService.isRequesterOf(request);

        return revisionStatus && isRequester;
    }

    onSuccess(response: Response) {
        console.log('success ', response);
        this.request = response.json();
        this.isUpdating = false;

        this.requestService.requestUpdateEvent(this.request);
    }

    onError(err) {
        console.log('error ', err);
        this.isUpdating = false;
    }

}
