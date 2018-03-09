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
import { Observable, BehaviorSubject, Subject } from 'rxjs/Rx';
import { RequestDetail } from './request-detail';
import { RequestBase } from './request-base';
import { RequestReviewFeedback } from './request-review-feedback';
import { PodiumEventMessage } from '../event/podium-event-message';
import { User } from '../user/user.model';
import { ReviewRound } from './review-round';
import { HttpHelper } from '../util/http-helper';
import { RequestTemplate } from './request-template';

@Injectable()
export class RequestService {

    // FIX ME Please refactor me.
    private resourceUrl = 'api/requests';
    private resourceSearchUrl = 'api/_search/requests';

    public onRequestUpdate: Subject<RequestBase> = new Subject();

    constructor(private http: Http) { }

    createDraft(): Observable<RequestBase> {
        return this.http.post(`${this.resourceUrl}/drafts`, null).map((res: Response) => {
            return res.json();
        });
    }

    findDrafts(req?: any): Observable<Response> {
        let options = HttpHelper.createRequestOption(req);
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
        let options = HttpHelper.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/status/Review/requester`, options).map((res: Response) => {
            return res;
        });
    }

    findMyApprovedRequests(req?: any): Observable<Response> {
        let options = HttpHelper.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/status/Approved/requester`, options).map((res: Response) => {
            return res;
        });
    }

    findMyDeliveryRequests(req?: any): Observable<Response> {
        let options = HttpHelper.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/status/Delivery/requester`, options).map((res: Response) => {
            return res;
        });
    }

    findCoordinatorReviewRequests(req?: any): Observable<Response> {
        let options = HttpHelper.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/status/Review/coordinator`, options).map((res: Response) => {
            return res;
        });
    }

    findCoordinatorApprovedRequests(req?: any): Observable<Response> {
        let options = HttpHelper.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/status/Approved/coordinator`, options).map((res: Response) => {
            return res;
        });
    }

    findCoordinatorDeliveryRequests(req?: any): Observable<Response> {
        let options = HttpHelper.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/status/Delivery/coordinator`, options).map((res: Response) => {
            return res;
        });
    }

    findAllReviewerRequests(req?: any): Observable<Response> {
        let options = HttpHelper.createRequestOption(req);
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
        let options = HttpHelper.createRequestOption(req);
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

    getTemplateByUuid(uuid: string): Observable<RequestTemplate> {
        return this.http.get(`${this.resourceUrl}/external/${uuid}`).map((res: Response) => {
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
        return this.http.put(`${this.resourceUrl}/${uuid}/review`, feedbackCopy);
    }

    rejectRequest(uuid: string, message: PodiumEventMessage): Observable<Response> {
        return this.http.post(`${this.resourceUrl}/${uuid}/reject`, message);
    }

    startRequestDelivery(uuid: string): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/${uuid}/startDelivery`);
    }

    closeRequest(uuid: string, message?: PodiumEventMessage) {
        return this.http.post(`${this.resourceUrl}/${uuid}/close`, message);
    }

    search(req?: any): Observable<Response> {
        let options = HttpHelper.createRequestOption(req);
        return this.http.get(this.resourceSearchUrl, options);
    }

    public requestUpdateEvent(requestBase: RequestBase) {
        this.onRequestUpdate.next(requestBase);
    }

    getLastReviewFeedbackByUser(request: RequestBase, user: User): RequestReviewFeedback {
        let lastFeedbackList = request.reviewRound.reviewFeedback;
        let lastFeedback: RequestReviewFeedback;
        if (lastFeedbackList) {
            lastFeedback = lastFeedbackList.find((feedback) => {
                 return feedback.reviewer.uuid === user.uuid;
            });
        }
        return lastFeedback;
    }
}
