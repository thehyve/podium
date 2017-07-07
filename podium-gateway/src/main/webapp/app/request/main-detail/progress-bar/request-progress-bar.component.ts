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
import { RequestBase } from '../../../shared/request/request-base';
import {
    RequestStatusOptions, RequestOverviewStatusOption,
    REQUEST_PROGRESSBAR_STATUSES_MAP, REQUEST_PROGRESSBAR_STATUSES, REQUEST_STATUSES
} from '../../../shared/request/request-status/request-status.constants';
import { RequestService } from '../../../shared/request/request.service';
import { Subscription } from 'rxjs';
import { RequestStatus } from '../../../shared/request/request-status/request-status';

@Component({
    selector: 'pdm-request-progress-bar',
    templateUrl: './request-progress-bar.component.html',
    styleUrls: ['request-progress-bar.scss']
})

export class RequestProgressBarComponent implements OnDestroy {

    @Input() request: RequestBase;
    requestStatusOptions: ReadonlyArray<RequestStatus>;
    requestProgressBarStatusOptions: ReadonlyArray<RequestStatus>;
    requestProgressBarStatusMap: { [token: string]: RequestStatus; };

    requestSubscription: Subscription;

    /**
     * List of final state status options
     */
    terminalStatuses: RequestOverviewStatusOption[] = [
        RequestOverviewStatusOption.Closed_Approved,
        RequestOverviewStatusOption.Cancelled,
        RequestOverviewStatusOption.Delivered,
        RequestOverviewStatusOption.Partially_Delivered,
        RequestOverviewStatusOption.Rejected,
    ];

    constructor(
        private requestService: RequestService
    ) {
        this.requestStatusOptions = REQUEST_STATUSES;
        this.requestProgressBarStatusOptions = REQUEST_PROGRESSBAR_STATUSES;
        this.requestProgressBarStatusMap = REQUEST_PROGRESSBAR_STATUSES_MAP;

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
     * Check whether the request is Closed
     *
     * @param request the current request
     * @param currentOrder order of the status being processed
     * @returns {boolean} true if the request status is closed
     */
    isClosed(request: RequestBase, currentOrder: number): boolean {
        if (this.terminalStatuses.indexOf(request.status) === -1) {
            return false;
        }

        let requestOrder = this.getRequestStatusOrder(request);
        return requestOrder === currentOrder;
    }

    isPartiallyDelivered(request: RequestBase) {
        return request.status === RequestOverviewStatusOption.Partially_Delivered;
    }

    isCancelled(request: RequestBase) {
        return request.status === RequestOverviewStatusOption.Cancelled;
    }

    isRevisionStatus(request: RequestBase): boolean {
        return request.status === RequestOverviewStatusOption.Revision;
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

        if (this.requestProgressBarStatusMap.hasOwnProperty(reqStatus)) {
            return this.requestProgressBarStatusMap[reqStatus].order;
        }

        switch (request.status) {
            case RequestOverviewStatusOption.Rejected:
                return 3; // Review element should be selected
            case RequestOverviewStatusOption.Approved:
                return 4;
            case RequestOverviewStatusOption.Closed_Approved:
                return 4;
            case RequestOverviewStatusOption.Cancelled:
                return 6;
            case RequestOverviewStatusOption.Partially_Delivered:
                return 6;
            case RequestOverviewStatusOption.Delivered:
                return 6;
            default:
                return 0;
        }
    }
}
