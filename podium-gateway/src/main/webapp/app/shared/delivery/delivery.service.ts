/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Injectable } from '@angular/core';
import { Http, BaseRequestOptions, URLSearchParams, Response } from '@angular/http';
import { Observable, Subject } from 'rxjs';
import { Delivery } from './delivery';
import { DeliveryReference } from './delivery-reference';
import { PodiumEventMessage } from '../event/podium-event-message';
import { DeliveryOutcome } from './delivery-outcome.constants';
import { RequestOutcome } from '../request/request-outcome';

@Injectable()
export class DeliveryService {

    private resourceUrl = 'api/requests';

    public onDeliveryUpdate: Subject<Delivery[]> = new Subject();
    public onDeliveries: Subject<Delivery[]> = new Subject();

    public static countDeliveriesWithOutcome(deliveries: Delivery[], outcome: DeliveryOutcome): number {
        return deliveries.filter((d) => {
            return d.outcome === outcome;
        }).length;
    }

    constructor(private http: Http) {}

    getDeliveries(uuid?: string): Observable<Delivery[]> {
        return this.http.get(`${this.resourceUrl}/${uuid}/deliveries`).map((res: Response) => {
            this.deliveriesFetchEvent(res.json());
            return res.json();
        });
    }

    releaseDelivery(requestUuid: string, deliveryUuid: string, deliveryReference: DeliveryReference): Observable<Response> {
        return this.http.post(`${this.resourceUrl}/${requestUuid}/deliveries/${deliveryUuid}/release`, deliveryReference)
            .map((res: Response) => {
                return res.json();
            });
    }

    cancelDelivery(requestUuid: string, deliveryUuid: string, message: PodiumEventMessage): Observable<Response> {
        return this.http.post(`${this.resourceUrl}/${requestUuid}/deliveries/${deliveryUuid}/cancel`, message)
            .map((res: Response) => {
                return res.json();
            });
    }

    receiveDelivery(requestUuid: string, deliveryUuid: string): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/${requestUuid}/deliveries/${deliveryUuid}/received`)
            .map((res: Response) => {
                return res.json();
            });
    }

    public deliveriesFetchEvent(deliveries: Delivery[]) {
        this.onDeliveries.next(deliveries);
    }

    /**
     * Requests can only be finalized when all deliveries have reached an end state (Received or Cancelled).
     *
     * @param deliveries The array of the request deliveries.
     * @returns {boolean} true if all deliveries have reached an end state, else false.
     */
    public canFinalizeRequest(deliveries: Delivery[]): boolean {
        if (!deliveries) {
            return false;
        }

        let totalDeliveries = deliveries.length;
        let numCancelled = DeliveryService.countDeliveriesWithOutcome(deliveries, DeliveryOutcome.Cancelled);
        let numReceived = DeliveryService.countDeliveriesWithOutcome(deliveries, DeliveryOutcome.Received);

        return (numCancelled + numReceived) === totalDeliveries;
    }

    getRequestDeliveryOutcome(deliveries: Delivery[]): RequestOutcome {
        if (!deliveries) {
            return RequestOutcome.None;
        }

        let totalDeliveries = deliveries.length;
        let numCancelled = DeliveryService.countDeliveriesWithOutcome(deliveries, DeliveryOutcome.Cancelled);
        let numReceived = DeliveryService.countDeliveriesWithOutcome(deliveries, DeliveryOutcome.Received);

        if (numReceived === totalDeliveries) {
            return RequestOutcome.Delivered;
        } else if (numCancelled === totalDeliveries) {
            return RequestOutcome.Cancelled;
        } else if ((numReceived + numCancelled) === totalDeliveries) {
            return RequestOutcome.Partially_Delivered;
        }

        return RequestOutcome.None;
    }
}
