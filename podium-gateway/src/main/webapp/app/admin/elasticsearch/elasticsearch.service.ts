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
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PdmElasticsearchService {

    constructor (private http: HttpClient) {}

    reindex(): Observable<HttpResponse<any>> {
        return this.http.get('podiumuaa/api/elasticsearch/index', {
            observe: 'response'
        });
    }

}
