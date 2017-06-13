/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import { Form } from '@angular/forms';
import { RequestBase } from '../../request-base';
import { RequestStatusOptions, RequestReviewStatusOptions } from '../../request-status/request-status.constants';
import { RequestAccessService } from '../../request-access.service';
import { RequestService } from '../../request.service';
import { Subscription } from 'rxjs';
import { Delivery } from '../../../delivery/delivery';
import { DeliveryService } from '../../../delivery/delivery.service';

@Component({
    selector: 'pdm-request-action-toolbar',
    templateUrl: './request-action-toolbar.component.html',
    styleUrls: ['request-action-toolbar.scss']
})

export class RequestActionToolbarComponent implements OnInit, OnDestroy {

    private status: string;
    private reviewStatus?: string;
    public requestStatus = RequestStatusOptions;
    public requestReviewStatus = RequestReviewStatusOptions;
    private requestSubscription: Subscription;
    private deliveriesSubscription: Subscription;

    public checks: any = {
        validation: false,
        canFinalize: false
    };

    @Input() form: Form;
    @Input() request: RequestBase;
    @Input() isUpdating: false;

    @Output() resetChange = new EventEmitter();
    @Output() cancelChange = new EventEmitter();
    @Output() rejectChange = new EventEmitter();
    @Output() saveDraftChange = new EventEmitter();
    @Output() saveRequestChange = new EventEmitter();
    @Output() submitDraftChange = new EventEmitter();
    @Output() approveRequestChange = new EventEmitter();
    @Output() submitRequestChange = new EventEmitter();
    @Output() validateRequestChange = new EventEmitter();
    @Output() requireRevisionChange = new EventEmitter();
    @Output() reviewAdviseApproved = new EventEmitter();
    @Output() reviewAdviseRejected = new EventEmitter();
    @Output() startDeliveryChange = new EventEmitter();
    @Output() closeRequestChange = new EventEmitter();
    @Output() finalizeRequestChange = new EventEmitter();

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private requestAccessService: RequestAccessService,
        private deliveryService: DeliveryService,
        private requestService: RequestService
    ) {
        this.jhiLanguageService.setLocations(['request', 'requestStatus']);
        this.requestSubscription = this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
            this.initializeStatuses();
        });

        this.deliveriesSubscription = this.deliveryService.onDeliveries.subscribe(
            (deliveries) => {
                this.canFinalizeRequest(deliveries);
            }
        );
    }

    ngOnInit() {
        this.initializeStatuses();
    }

    ngOnDestroy() {
        if (this.requestSubscription) {
            this.requestSubscription.unsubscribe();
        }

        if (this.deliveriesSubscription) {
            this.deliveriesSubscription.unsubscribe();
        }
    }

    canFinalizeRequest(requestDeliveries: Delivery[]) {
        if (!requestDeliveries) {
            this.checks.canFinalize = false;
        }

        this.checks.canFinalize = this.deliveryService.canFinalizeRequest(requestDeliveries);
    }

    initializeStatuses() {
        this.status = this.request.status.toString();
        if (this.request.requestReview) {
            this.reviewStatus = this.request.requestReview.status.toString();
        }
    }

    isStatus(status): boolean {
        // Status value comes as enumeration index
        return this.status === RequestStatusOptions[status];
    }

    isReviewStatus(status): boolean {
        // Status value comes as enumeration index
        return this.reviewStatus === RequestReviewStatusOptions[status];
    }

    isRequestCoordinator(): boolean {
        return this.requestAccessService.isCoordinatorFor(this.request);
    }

    isRequestReviewer(): boolean {
        return this.requestAccessService.isReviewerFor(this.request);
    }

    isRequestingResearcher(): boolean {
        return this.requestAccessService.isRequesterOf(this.request);
    }

    isReviewable(): boolean {
        let lastFeedbacks = this.requestService.getLastReviewFeedbacks(this.request.reviewRounds);
        return this.requestAccessService.isReviewable(lastFeedbacks);
    }

    saveDraft() {
        this.saveDraftChange.emit(true);
    }

    saveRequest() {
        this.saveRequestChange.emit(true);
    }

    submitDraft() {
        this.submitDraftChange.emit(true);
    }

    cancel() {
        this.cancelChange.emit(true);
    }

    resetForm() {
        this.resetChange.emit(true);
    }

    rejectRequest() {
        this.rejectChange.emit(true);
    }

    validateRequest() {
        this.validateRequestChange.emit(true);
    }

    requireRevision() {
        this.requireRevisionChange.emit(true);
    }

    approveRequest() {
        this.approveRequestChange.emit(true);
    }

    submitRequest() {
        this.submitRequestChange.emit(true);
    }

    reviewApproved() {
        this.reviewAdviseApproved.emit(true);
    }

    reviewRejected() {
        this.reviewAdviseRejected.emit(true);
    }

    startDelivery() {
        this.startDeliveryChange.emit(true);
    }

    closeRequest() {
        this.closeRequestChange.emit(true);
    }

    finalizeRequest() {
        this.finalizeRequestChange.emit(true);
    }
}
