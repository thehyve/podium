/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, Input, OnDestroy } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import { RequestBase } from '../../../shared/request/request-base';
import {
    RequestStatus,
    REQUEST_STATUSES,
    REQUEST_STATUSES_MAP,
    REQUEST_REVIEW_STATUSES,
    REQUEST_REVIEW_STATUSES_MAP
} from '../../../shared/request/request-status';
import {
    RequestStatusOptions,
    RequestReviewStatusOptions
} from '../../../shared/request/request-status/request-status.constants';
import { RequestAccessService } from '../../../shared/request/request-access.service';
import { RequestService } from '../../../shared/request/request.service';
import { RequestReviewDecision } from '../../../shared/request/request-review-decision';
import { Subscription } from 'rxjs';

@Component({
    selector: 'pdm-request-progress-bar',
    templateUrl: './request-progress-bar.component.html',
    styleUrls: ['request-progress-bar.scss']
})

export class RequestProgressBarComponent implements OnDestroy {

    @Input() request: RequestBase;
    requestStatusOptions: ReadonlyArray<RequestStatus>;
    requestStatusMap: { [token: string]: RequestStatus; };
    requestReviewStatusOptions: ReadonlyArray<RequestStatus>;
    requestReviewStatusMap: { [token: string]: RequestStatus; };

    requestSubscription: Subscription;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private requestAccessService: RequestAccessService,
        private requestService: RequestService) {
        jhiLanguageService.setLocations(['request', 'requestStatus']);
        this.requestStatusOptions = REQUEST_STATUSES;
        this.requestStatusMap = REQUEST_STATUSES_MAP;
        this.requestReviewStatusOptions = REQUEST_REVIEW_STATUSES;
        this.requestReviewStatusMap = REQUEST_REVIEW_STATUSES_MAP;

        this.requestSubscription = this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
        });
    }

    ngOnDestroy() {
        if (this.requestSubscription) {
            this.requestSubscription.unsubscribe();
        }
    }

    /**
     * Check whether the current request status is active
     *
     * @param request the current request
     * @param currentOrder order of the status being processed
     * @returns {boolean} status is active or not
     */
    isActive(request: RequestBase, currentOrder: number): boolean {
        let requestOrder = this.getRequestStatusOrder(request);
        return requestOrder === currentOrder;
    }

    /**
     * Check whether the request status being processed has been completed
     *
     * @param request the current request
     * @param currentOrder order of the status being processed
     * @returns {boolean} status has been completed or not
     */
    isCompleted(request: RequestBase, currentOrder: number): boolean {
        let requestOrder = this.getRequestStatusOrder(request);
        return requestOrder > currentOrder;
    }

    /**
     * Check whether the request is terminated
     * All closed states return an order of -1
     *
     * @param request the current request
     * @returns {boolean} true if the request status is closed
     */
    isTerminatedReview(request: RequestBase): boolean {
        return this.getRequestStatusOrder(request) === -1 &&
            request.requestReview.decision === RequestReviewDecision.Rejected;
    }

    isRevisionStatus(request: RequestBase): boolean {
        return this.requestAccessService.isRequestReviewStatus(request, RequestReviewStatusOptions.Revision);
    }

    /**
     * Find the order of the current request
     * When the request is in Review, return the status order of the review process.
     * When the request has a different status, return that status order
     *
     * @param request the current request
     * @returns {number} order of the current status
     */
    getRequestStatusOrder(request: RequestBase): number {
        let reqStatus = request.status;
        let reviewStatus = RequestStatusOptions.Review;
        if (reqStatus.toString() === RequestStatusOptions[reviewStatus]) {
            let reqReviewStatus: RequestReviewStatusOptions = request.requestReview.status;
            // Return requestReviewStatusOrder
            if (this.requestReviewStatusMap.hasOwnProperty(reqReviewStatus)) {
                return this.requestReviewStatusMap[reqReviewStatus].order;
            }
        } else {
            // Not Review status - return requestStatusOrder
            if (this.requestStatusMap.hasOwnProperty(reqStatus)) {
                return this.requestStatusMap[reqStatus].order;
            }
        }

        return 0;
    }
}
