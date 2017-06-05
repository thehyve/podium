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
import { Http, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class Verify {

    constructor(private http: Http) {
    }

    get(key: string): Observable<any> {
        let params: URLSearchParams = new URLSearchParams();
        params.set('key', key);

        return this.http.get('podiumuaa/api/verify', {
            search: params
        }).map((res: Response) => res);
    }

    renew(key: string): Observable<any> {
        let params: URLSearchParams = new URLSearchParams();
        params.set('key', key);

        return this.http.get('podiumuaa/api/reverify', {
            search: params
        }).map((res: Response) => res);
    }
}
