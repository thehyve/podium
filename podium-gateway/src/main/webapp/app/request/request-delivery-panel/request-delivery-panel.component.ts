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

@Component({
    selector: 'pdm-request-delivery-panel',
    templateUrl: './request-delivery-panel.component.html',
    styleUrls: ['request-delivery-panel.scss']
})

export class RequestDeliveryPanelComponent implements OnInit, OnDestroy {

    @Input()
    request: RequestBase;

    requestDeliveries: Delivery[];
    requestSubscription: Subscription;
    primaryStateOptions: any = DeliveryStateOptions.primary;
    secondaryStateOptions: any = DeliveryStateOptions.secondary;

    constructor(
        private jhiLanguageService: JhiLanguageService,
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
    }

    getDeliveries()  {
        this.deliveryService.getDeliveries(this.request.uuid)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    onSuccess(res: Delivery[]) {
        console.log('SUCCESS DELIVERIES ', res);
        this.requestDeliveries = res;
    }

    onError(err: any) {
        console.log('ERRORR ', err);
    }

    get self() {
        return this;
    }

    releaseType(delivery: Delivery) {
        console.log('Releasing ', delivery);
    }

    receiveType(delivery: Delivery) {
        console.log('Receiving', delivery);
    }

    cancelType(delivery: Delivery) {
        console.log('Cancelling ', delivery);
    }

    performAction(action: string, delivery: Delivery) {
        console.log('Act ', action, delivery);
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
