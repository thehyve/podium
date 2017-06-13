/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component } from '@angular/core';
import { DeliveryStatusUpdateAction } from './delivery-update-action';
import { RequestBase, RequestService } from '../request';
import { Response } from '@angular/http';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { DeliveryReference } from '../delivery/delivery-reference';
import { Delivery } from '../delivery/delivery';
import { PodiumEventMessage } from '../event/podium-event-message';
import { DeliveryService } from '../delivery/delivery.service';

@Component({
    selector: 'pdm-delivery-status-update',
    templateUrl: './delivery-update.component.html',
    styleUrls: ['delivery-update.scss']
})

export class DeliveryStatusUpdateDialogComponent {

    request: RequestBase;
    delivery: Delivery;
    statusUpdateAction: DeliveryStatusUpdateAction;
    statusUpdateOptions = DeliveryStatusUpdateAction;
    releaseMessage: DeliveryReference = new DeliveryReference();
    cancelledMessage: PodiumEventMessage = new PodiumEventMessage();

    constructor(
        private deliveryService: DeliveryService,
        private activeModal: NgbActiveModal
    ) {

    }

    close() {
        this.activeModal.dismiss('closed');
    }

    /**
     * Confirm and submit a status update with a message
     *
     * returns an unsubscribed observable with the action
     */
    confirmStatusUpdate() {
        if (this.statusUpdateAction === DeliveryStatusUpdateAction.Release) {
            return this.deliveryService.releaseDelivery(this.request.uuid, this.delivery.uuid, this.releaseMessage)
                .subscribe((res) => this.onSuccess(res));
        }

        if (this.statusUpdateAction === DeliveryStatusUpdateAction.Cancel) {
            return this.deliveryService.cancelDelivery(this.request.uuid, this.delivery.uuid, this.cancelledMessage)
                .subscribe((res) => this.onSuccess(res));
        }

        this.activeModal.dismiss(new Error('Unknown status update action'));
    }

    getHeaderTranslation() {
        let requestId = this.request.id;
        return '{requestId: \'' + requestId + '\'}';
    };

    getSubmitTooltip(): string {
        if (this.statusUpdateAction === DeliveryStatusUpdateAction.Release) {
            return 'Please provide a reference indicating release details.';
        } else if (this.statusUpdateAction === DeliveryStatusUpdateAction.Cancel) {
            return 'Please provide at least a summary of your message.';
        }

        return '';
    }

    onSuccess(res: Response) {
        this.activeModal.close(true);
    }
}
