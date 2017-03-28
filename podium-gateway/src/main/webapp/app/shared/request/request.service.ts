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

import { RequestDetail } from './request-detail';
import { RequestBase } from './request-base';

@Injectable()
export class RequestService {

    private resourceUrl = 'api/requests';
    private resourceSearchUrl = 'api/_search/requests';

    constructor(private http: Http) { }

    createDraft(): Observable<RequestBase> {
        return this.http.post(`${this.resourceUrl}/drafts`, null).map((res: Response) => {
            return res.json();
        });
    }

    findDrafts(): Observable<RequestBase[]> {
        return this.http.get(`${this.resourceUrl}/drafts`).map((res: Response) => {
            return res.json();
        });
    }

    saveDraft(requestBase: RequestBase): Observable<RequestBase> {
        let draftCopy: RequestBase = Object.assign({}, requestBase);
        return this.http.put(`${this.resourceUrl}/drafts`, draftCopy).map((res: Response) => {
            return res.json();
        });
    }

    /**
     * Submits the draft request and generates a new request for each of the
     * selected organisations.
     *
     * @param requestBase the draft to submit
     * @returns the list of generated requests.
     */
    submitDraft(requestBase: RequestBase): Observable<RequestBase[]> {
        let draftCopy: RequestBase = Object.assign({}, requestBase);
        return this.http.put(`${this.resourceUrl}/drafts`, draftCopy).flatMap((res: Response) => {
            let savedDraft: RequestBase = Object.assign({}, res.json());
            let uuid = savedDraft.uuid;
            return this.http.get(`${this.resourceUrl}/drafts/${uuid}/submit`, draftCopy).map((response: Response) => {
                return response.json();
            });
        });
    }

    findDraftByUuid(uuid: string): Observable<RequestDetail> {
        return this.http.get(`${this.resourceUrl}/uuid/${uuid}`).map((res: Response) => {
            return res.json();
        });
    }

    delete(uuid: number): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${uuid}`);
    }

    search(req?: any): Observable<Response> {
        let options = this.createRequestOption(req);
        return this.http.get(this.resourceSearchUrl, options);
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
