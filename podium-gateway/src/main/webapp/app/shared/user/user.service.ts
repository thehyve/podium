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
import { Http, Response, URLSearchParams, BaseRequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { User } from './user.model';

@Injectable()
export class UserService {
    private resourceUrl = 'podiumuaa/api/users';
    private resourceSearchUrl = 'podiumuaa/api/_search/users';
    private resourceSuggestUrl = 'podiumuaa/api/_suggest/users';

    constructor(private http: Http) {
    }

    create(user: User): Observable<Response> {
        return this.http.post(this.resourceUrl, user);
    }

    update(user: User): Observable<Response> {
        return this.http.put(this.resourceUrl, user);
    }

    unlock(user: User): Observable<Response> {
        return this.http.put(`${this.resourceUrl}/uuid/${user.uuid}/unlock`, {});
    }

    find(login: string): Observable<User> {
        return this.http.get(`${this.resourceUrl}/${login}`).map((res: Response) => res.json());
    }

    findByUuid(uuid: string): Observable<User> {
        return this.http.get(`${this.resourceUrl}/uuid/${uuid}`).map((res: Response) => res.json());
    }

    query(req?: any): Observable<Response> {
        let params: URLSearchParams = new URLSearchParams();
        if (req) {
            params.set('page', req.page);
            params.set('size', req.size);
            if (req.sort) {
                params.paramsMap.set('sort', req.sort);
            }
            params.set('filter', req.filter);
        }

        let options = {
            search: params
        };

        return this.http.get(this.resourceUrl, options);
    }

    search(req?: any): Observable<Response> {
        let options = this.createRequestOption(req);
        return this.http.get(`${this.resourceSearchUrl}`, options).map((res: Response) => res);
    }

    suggest(req?: any): Observable<Response> {
        let options = this.createRequestOption(req);
        return this.http.get(`${this.resourceSuggestUrl}`, options).map((res: Response) => res.json());
    }

    delete(login: string): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${login}`);
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
