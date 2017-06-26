/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import { Subscription } from 'rxjs';
import { RequestBase } from '../../shared/request/request-base';
import { RequestService } from '../../shared/request/request.service';
import { RequestAccessService } from '../../shared/request/request-access.service';
import { Delivery } from '../../shared/delivery/delivery';
import { DeliveryService } from '../../shared/delivery/delivery.service';
import { DeliveryStateOptions } from '../../shared/delivery/delivery-state-options.constants';
import { DeliveryStatusUpdateAction } from '../../shared/delivery-update/delivery-update-action';
import { DeliveryStatusUpdateDialogComponent } from '../../shared/delivery-update/delivery-update.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DeliveryStatus } from '../../shared/delivery/delivery-status.constants';
import { DeliveryOutcome } from '../../shared/delivery/delivery-outcome.constants';
import { RequestStatusOptions } from '../../shared/request/request-status/request-status.constants';
import { RequestOutcome } from '../../shared/request/request-outcome';

@Component({
    selector: 'pdm-request-delivery-panel',
    templateUrl: './request-delivery-panel.component.html',
    styleUrls: ['request-delivery-panel.scss']
})

export class RequestDeliveryPanelComponent implements OnInit, OnDestroy {

    @Input()
    request: RequestBase;

    public requestDeliveries: Delivery[];
    public requestSubscription: Subscription;
    public deliverySubscription: Subscription;
    public primaryStateOptions: any = DeliveryStateOptions.primary;
    public secondaryStateOptions: any = DeliveryStateOptions.secondary;
    public iconStateOptions: any = DeliveryStateOptions.icons;
    public deliveryStatusOptions = DeliveryStatus;

    public isUpdating = false;

    constructor(
        private modalService: NgbModal,
        private requestAccessService: RequestAccessService,
        private requestService: RequestService,
        private deliveryService: DeliveryService
    ) {
        this.requestSubscription = this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
            this.getDeliveries();
        });

        this.deliverySubscription = this.deliveryService.onDeliveryUpdate.subscribe((deliveries: Delivery[]) => {
            this.requestDeliveries = deliveries;
        });
    }

    ngOnInit() {
        this.getDeliveries();
    }

    /**
     * Subscription clean up to prevent memory leaks
     */
    ngOnDestroy() {
        if (this.requestSubscription) {
            this.requestSubscription.unsubscribe();
        }

        if (this.deliverySubscription) {
            this.deliverySubscription.unsubscribe();
        }
    }


    /**
     *  Check if deliveries exists in a request by checking if request status in on Delivery or request outcome is
     *  Delivered, Partially_Delivered or Cancelled.
     * @param request
     * @returns {boolean}
     */
    private deliveriesExistIn(request): boolean {
        return  request.status === RequestStatusOptions.Delivery ||
                request.outcome === RequestOutcome.Delivered ||
                request.outcome === RequestOutcome.Partially_Delivered ||
                request.outcome === RequestOutcome.Cancelled;
    }

    /**
     * Fetch all deliveries for a request by request UUID.
     */
    getDeliveries()  {
        if (this.request !== null) {
            if (this.deliveriesExistIn(this.request)) {
                this.deliveryService.getDeliveries(this.request.uuid)
                    .subscribe(
                        (res) => this.onSuccess(res)
                    );
            }
        }
    }

    /**
     * Set the deliveries belonging to the request after a successful fetch.
     * @param res
     */
    onSuccess(res: Delivery[]) {
        this.deliveryService.deliveriesFetchEvent(res);
        this.requestDeliveries = res;
    }

    /**
     * Fetch all the deliveries after a successful update of a delivery.
     */
    onSuccessUpdate() {
        this.getDeliveries();
    }

    /**
     * Mark a delivery as Released.
     *
     * @param delivery The delivery to release
     */
    releaseType(delivery: Delivery) {
        this.confirmStatusUpdateModal(this.request, delivery, DeliveryStatusUpdateAction.Release);
    }

    /**
     * Mark a delivery as Received.
     *
     * @param delivery The delivery to receive
     */
    receiveType(delivery: Delivery) {
        this.deliveryService.receiveDelivery(this.request.uuid, delivery.uuid)
            .subscribe((res) => this.onSuccessUpdate());
    }

    /**
     * Mark a delivery as Cancelled.
     * A modal will be presented to ask for feedback from the organisation coordinator.
     *
     * @param delivery The delivery to cancel
     */
    cancelType(delivery: Delivery) {
        this.confirmStatusUpdateModal(this.request, delivery, DeliveryStatusUpdateAction.Cancel);
    }

    deliveryIsReceived(delivery: Delivery) {
        return delivery.outcome === DeliveryOutcome.Received;
    }

    deliveryIsReleased(delivery: Delivery) {
        return delivery.status === DeliveryStatus.Released;
    }

    deliveryIsCancelled(delivery: Delivery) {
        return delivery.outcome === DeliveryOutcome.Cancelled;
    }

    hasDeliveries(): boolean {
        return this.requestDeliveries && this.requestDeliveries.length > 0;
    }

    /**
     * Get the notes for a delivery.
     * Depending on the status of the request these are either located in the reference or in a historical event
     * with the data.targetStatus 'Closed'.
     *
     * @param delivery The delivery to fetch the notes for.
     * @returns {any} An object holding the notes.
     */
    getNotes(delivery: Delivery): any {

        /**
         * When the delivery is cancelled (Closed) return the reason for closing the delivery type.
         * Otherwise return the reference that was given while Releasing the delivery type.
         */
        if (delivery.status === DeliveryStatus.Closed) {
            switch (delivery.outcome) {
                case DeliveryOutcome.Cancelled:
                    let cancelledEvents
                        = this.getHistoricEventForTargetStatus(delivery, DeliveryStatus.Closed.toString());
                    if (cancelledEvents) {
                        return {
                            'summary': cancelledEvents[cancelledEvents.length - 1].data.messageSummary,
                            'description': cancelledEvents[cancelledEvents.length - 1].data.messageDescription
                        };
                    }
                    break;
                case DeliveryOutcome.Received:
                    return {
                        'summary': delivery.reference
                    };
            }
        }

        if (delivery.status === DeliveryStatus.Released) {
            return {
                'summary': delivery.reference
            };
        }
    }

    getHistoricEventForTargetStatus(delivery: Delivery, targetStatus: string) {
        return delivery.historicEvents.filter(e => {
            return e.data.targetStatus === targetStatus;
        });
    }

    /**
     * Check whether a delivery is actionable.
     * Organisation Coordinators can perform actions on all but Received requests
     * Requesters can only mark the request as Received after it has been Released.
     *
     * @param delivery A delivery
     * @returns boolean true if a delivery is actionable for the current user
     */
    isActionable(delivery: Delivery): boolean {

        // Once a delivery process has been closed it is no longer actionable
        if (delivery.status === DeliveryStatus.Closed) {
            return false;
        }

        // Organisation Coordinator
        if (this.requestAccessService.isCoordinatorFor(this.request)) {
            return true;
        }

        // Requester
        if (this.requestAccessService.isRequesterOf(this.request)) {
            return delivery.status === DeliveryStatus.Released;
        }

        return false;
    }

    isOnlyRequester(): boolean {
        return this.requestAccessService.isRequesterOf(this.request) &&
            !this.requestAccessService.isCoordinatorFor(this.request);
    }

    confirmStatusUpdateModal(request: RequestBase, delivery: Delivery, action: DeliveryStatusUpdateAction) {
        let modalRef = this.modalService.open(DeliveryStatusUpdateDialogComponent, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.request = request;
        modalRef.componentInstance.delivery = delivery;
        modalRef.componentInstance.statusUpdateAction = action;
        modalRef.result.then(result => {
            if (result) {
                this.getDeliveries();
            }
            this.isUpdating = false;
        }, (reason) => {
            console.error('Err on delivery update: ', reason);
            this.isUpdating = false;
        });
    }

    performAction(action: string, delivery: Delivery) {
        switch (action) {
            case 'release':
                this.releaseType(delivery);
                break;
            case 'receive':
                this.receiveType(delivery);
                break;
            case 'cancel':
                this.cancelType(delivery);
                break;
            default:
                break;
        }

    }

}
