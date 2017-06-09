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
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { RequestBase } from '../../../shared/request/request-base';
import { Delivery } from '../../../shared/delivery/delivery';
import { Response } from '@angular/http';
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

    confirmRequestFinalize() {
        this.requestService.finalizeRequest(this.request.uuid)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    onSuccess(res: Response) {
        console.log('Success finalizing ', res);
        this.request = res.json();
        this.requestService.requestUpdateEvent(this.request);
        this.activeModal.close();
    }

    onError(err: Response) {
        console.log('err when finalizing ', err);
    }

    close() {
        this.activeModal.dismiss('closed');
    }

    getHeaderTranslation() {
        let requestId = this.request.id;
        return '{requestId: \'' + requestId + '\'}';
    };


}
