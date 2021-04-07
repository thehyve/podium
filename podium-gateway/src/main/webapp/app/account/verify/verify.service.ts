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

@Injectable({ providedIn: 'root' })
export class Verify {

    constructor (private http: HttpClient) {}

    get(key: string): Observable<any> {
        return this.http.get('podiumuaa/api/verify', {
            params: { key }
        });
    }

    renew(key: string): Observable<any> {
        return this.http.get('podiumuaa/api/reverify', {
            params: { key }
        });
    }
}
