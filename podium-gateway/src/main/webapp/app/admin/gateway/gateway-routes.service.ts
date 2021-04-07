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
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Rx';
import { GatewayRoute } from './gateway-route.model';

@Injectable({ providedIn: 'root' })
export class GatewayRoutesService {
    constructor(private http: HttpClient) { }

    findAll(): Observable<GatewayRoute[]> {
        return this.http.get<GatewayRoute[]>('api/gateway/routes/');
    }
}
