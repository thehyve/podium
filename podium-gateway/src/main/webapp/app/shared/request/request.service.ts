/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Injectable, EventEmitter } from '@angular/core';
import { Http, Response, URLSearchParams, BaseRequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { RequestDetail } from './request-detail';
import { RequestBase } from './request-base';
import { RequestReviewFeedback } from './request-review-feedback';
import { PodiumEventMessage } from '../event/podium-event-message';

@Injectable()
export class RequestService {

    private resourceUrl = 'api/requests';
    private resourceSearchUrl = 'api/_search/requests';

    public onRequestUpdate: EventEmitter<RequestBase> = new EventEmitter<RequestBase>(false);

    constructor(private http: Http) { }

    createDraft(): Observable<RequestBase> {
        return this.http.post(`${this.resourceUrl}/drafts`, null).map((res: Response) => {
            return res.json();
        });
    }

    findDrafts(req?: any): Observable<Response> {
        let options = this.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/drafts`, options).map((res: Response) => {
            return res;
        });
    }

    saveDraft(requestBase: RequestBase): Observable<RequestBase> {
        let draftCopy: RequestBase = Object.assign({}, requestBase);
        return this.http.put(`${this.resourceUrl}/drafts`, draftCopy).map((res: Response) => {
            return res.json();
        });
    }

    findMyReviewRequests(req?: any): Observable<Response> {
        let options = this.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/status/Review/requester`, options).map((res: Response) => {
            return res;
        });
    }

    findCoordinatorReviewRequests(req?: any): Observable<Response> {
        let options = this.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/status/Review/coordinator`, options).map((res: Response) => {
            return res;
        });
    }

    findAllReviewerRequests(req?: any): Observable<Response> {
        let options = this.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/reviewer`, options).map((res: Response) => {
            return res;
        });
    }

    /**
     * Submits the draft request and generates a new request for each of the
     * selected organisations.
     *
     * @param uuid the uuid of the draft to submit
     * @returns the list of generated requests.
     */
    submitDraft(uuid: string): Observable<RequestBase[]> {
        return this.http.get(`${this.resourceUrl}/drafts/${uuid}/submit`).map((response: Response) => {
            return response.json();
        });
    }

    /**
     * Save the revision details during a revision phase
     *
     * @param requestBase the request to save
     * @returns {Observable<Response>}
     */
    saveRequestRevision(requestBase: RequestBase): Observable<Response> {
        let requestCopy: RequestBase = Object.assign({}, requestBase);
        return this.http.put(`${this.resourceUrl}`, requestCopy).map((response: Response) => {
            return response.json();
        });
    }

    submitRequestRevision(uuid: string): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/${uuid}/submit`).map((response: Response) => {
            return response.json();
        });
    }

    findSubmittedRequests(req?: any): Observable<Response> {
        let options = this.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/status/Review`, options).map((res: Response) => {
            return res;
        });
    }

    findDraftByUuid(uuid: string): Observable<RequestDetail> {
        return this.http.get(`${this.resourceUrl}/drafts/${uuid}`).map((res: Response) => {
            return res.json();
        });
    }

    findByUuid(uuid: string): Observable<RequestDetail> {
        return this.http.get(`${this.resourceUrl}/${uuid}`).map((res: Response) => {
            return res.json();
        });
    }

    deleteDraft(uuid: string): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/drafts/${uuid}`);
    }

    /**
     * Process functions
     */
    validateRequest(uuid: string): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/${uuid}/validate`);
    }

    requestRevision(uuid: string, message: PodiumEventMessage): Observable<Response> {
        return this.http.post(`${this.resourceUrl}/${uuid}/requestRevision`, message);
    }

    approveRequest(uuid: string): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/${uuid}/approve`);
    }

    submitReview(uuid: string, reviewFeedback: RequestReviewFeedback) {
        let feedbackCopy: RequestReviewFeedback = Object.assign({}, reviewFeedback);
        return this.http.post(`${this.resourceUrl}/${uuid}/review`, feedbackCopy);
    }

    rejectRequest(uuid: string, message: PodiumEventMessage): Observable<Response> {
        return this.http.post(`${this.resourceUrl}/${uuid}/reject`, message);
    }

    search(req?: any): Observable<Response> {
        let options = this.createRequestOption(req);
        return this.http.get(this.resourceSearchUrl, options);
    }

    public requestUpdateEvent(requestBase: RequestBase) {
        this.onRequestUpdate.emit(requestBase);
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
