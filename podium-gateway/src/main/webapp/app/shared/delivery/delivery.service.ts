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
import { Observable } from 'rxjs';
import { Delivery } from './delivery';
import { DeliveryReference } from './delivery-reference';
import { PodiumEventMessage } from '../event/podium-event-message';

@Injectable()
export class DeliveryService {

    private resourceUrl = 'api/requests';

    constructor(private http: Http) {}

    getDeliveries(uuid?: string): Observable<Delivery[]> {
        return this.http.get(`${this.resourceUrl}/${uuid}/deliveries`).map((res: Response) => {
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

    receivedDelivery(requestUuid: string, deliveryUuid: string): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/${requestUuid}/deliveries/${deliveryUuid}/received`)
            .map((res: Response) => {
                return res.json();
            });
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
