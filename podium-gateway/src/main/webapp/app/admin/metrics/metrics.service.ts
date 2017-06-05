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
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class PdmMetricsService {

    constructor(private http: Http) {
    }

    getMetrics(): Observable<any> {
        return this.http.get('management/metrics').map((res: Response) => res.json());
    }

    threadDump(): Observable<any> {
        return this.http.get('management/dump').map((res: Response) => res.json());
    }
}
