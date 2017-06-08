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
        private jhiLanguageService: JhiLanguageService,
        private modalService: NgbModal,
        private requestAccessService: RequestAccessService,
        private requestService: RequestService,
        private deliveryService: DeliveryService
    ) {
        jhiLanguageService.addLocation('delivery');
        jhiLanguageService.addLocation('deliveryStatus');
        jhiLanguageService.addLocation('requestType');

        this.requestSubscription = this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
            this.getDeliveries();
        });

        this.deliverySubscription = this.deliveryService.onDeliveryUpdate.subscribe((deliveries: Delivery[]) => {
            this.requestDeliveries = deliveries;
        });
    }

    ngOnInit() {
        if (this.request != null) {
            this.getDeliveries();
        }
    }

    ngOnDestroy() {
        if (this.requestSubscription) {
            this.requestSubscription.unsubscribe();
        }

        if (this.deliverySubscription) {
            this.deliverySubscription.unsubscribe();
        }
    }

    getDeliveries()  {
        this.deliveryService.getDeliveries(this.request.uuid)
            .subscribe(
                (res) => this.onSuccess(res)
            );
    }

    onSuccess(res: Delivery[]) {
        this.requestDeliveries = res;
    }

    onSuccessUpdate(res: Delivery) {
        console.log('Success update ', res);
        this.deliveryService.deliveryUpdateEvent(res);
    }

    releaseType(delivery: Delivery) {
        this.confirmStatusUpdateModal(this.request, delivery, DeliveryStatusUpdateAction.Release);
    }

    receiveType(delivery: Delivery) {
        this.deliveryService.receiveDelivery(this.request.uuid, delivery.uuid)
            .subscribe((res) => this.onSuccessUpdate(res.json()));
    }

    cancelType(delivery: Delivery) {
        this.confirmStatusUpdateModal(this.request, delivery, DeliveryStatusUpdateAction.Cancel);
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
            this.isUpdating = false;
        });
    }

    performAction(action: string, delivery: Delivery) {
        switch(action) {
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
