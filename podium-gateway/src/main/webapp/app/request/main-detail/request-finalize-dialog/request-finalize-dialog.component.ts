/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { RequestBase } from '../../../shared/request/request-base';
import { Delivery } from '../../../shared/delivery/delivery';
import { RequestService } from '../../../shared/request/request.service';
import { RequestOutcome } from '../../../shared/request/request-outcome';
import { DeliveryService } from '../../../shared/delivery/delivery.service';

@Component({
    selector: 'pdm-request-finalize-dialog',
    templateUrl: './request-finalize-dialog.component.html',
    styleUrls: ['request-finalize-dialog.scss']
})

export class RequestFinalizeDialogComponent implements OnInit {
    public request: RequestBase;
    public deliveries: Delivery[];
    public expectedOutcome: RequestOutcome;
    public outcomeOptions = RequestOutcome;

    constructor(
        private requestService: RequestService,
        private deliveryService: DeliveryService,
        private activeModal: NgbActiveModal
    ) {

    }

    ngOnInit() {
        this.expectedOutcome = this.deliveryService.getRequestDeliveryOutcome(this.deliveries);
    }

    /**
     * Finalize a request using its UUID and mark as Closed.
     * The final request outcome will be set upon closing.
     */
    confirmRequestFinalize() {
        this.requestService.closeRequest(this.request.uuid)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    /**
     * After successfully finalizing a request emit the final request to the request service
     * and close the active modal.
     *
     * @param res the response holding the request
     */
    onSuccess(res: HttpResponse<RequestBase>) {
        console.log('Success finalizing ', res);
        this.request = res.body;
        this.requestService.requestUpdateEvent(this.request);
        this.activeModal.close();
    }

    onError(err: HttpErrorResponse) {
        console.log('err when finalizing ', err);
    }

    /**
     * Close the active request finalization modal
     */
    close() {
        this.activeModal.dismiss('closed');
    }

    /**
     * Get the i18n translation object for the requestId
     * @returns {string} the formatted object holding the requestId
     */
    getHeaderTranslation() {
        return { requestId: this.request.id };
    };


}
