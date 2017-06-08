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

@Injectable()
export class DeliveryService {

    private resourceUrl = 'api/requests';

    public onDeliveryUpdate: Subject<Delivery> = new Subject();
    public onDeliveries: Subject<Delivery[]> = new Subject();

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

    public deliveryUpdateEvent(delivery: Delivery) {
        this.onDeliveryUpdate.next(delivery);
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
        let numCancelled = deliveries.filter((d) => {
            return d.outcome === DeliveryOutcome.Cancelled;
        }).length;

        let numReceived = deliveries.filter((d) => {
            return d.outcome === DeliveryOutcome.Received;
        }).length;

        return (numCancelled + numReceived) === totalDeliveries;
    }

    private createRequestOption(req?: any): BaseRequestOptions {
        let options: BaseRequestOptions = new BaseRequestOptions();
        if (req) {
            let params: URLSearchParams = new URLSearchParams();
            params.set('page', req.page);
            params.set('size', req.size);
            if (req.sort) {
                params.paramsMap.set('sort', req.sort);
            }
            params.set('query', req.query);

            options.search = params;
        }
        return options;
    }
}
