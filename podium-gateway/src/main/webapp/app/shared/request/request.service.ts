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
import { Observable, Subject } from 'rxjs/Rx';
import { RequestDetail } from './request-detail';
import { RequestBase } from './request-base';
import { RequestReviewFeedback } from './request-review-feedback';
import { PodiumEventMessage } from '../event/podium-event-message';
import { User } from '../user/user.model';
import { RequestTemplate } from './request-template';

@Injectable({ providedIn: 'root' })
export class RequestService {

    // FIX ME Please refactor me.
    private resourceUrl = 'api/requests';

    public onRequestUpdate: Subject<RequestBase> = new Subject();

    constructor(private http: HttpClient) { }

    createDraft(): Observable<RequestBase> {
        return this.http.post<RequestBase>(`${this.resourceUrl}/drafts`, null);
    }

    saveDraft(requestBase: RequestBase): Observable<RequestBase> {
        let draftCopy: RequestBase = Object.assign({}, requestBase);
        return this.http.put<RequestBase>(`${this.resourceUrl}/drafts`, draftCopy);
    }

    /**
     * Submits the draft request and generates a new request for each of the
     * selected organisations.
     *
     * @param uuid the uuid of the draft to submit
     * @returns the list of generated requests.
     */
    submitDraft(uuid: string): Observable<RequestBase[]> {
        return this.http.get<RequestBase[]>(`${this.resourceUrl}/drafts/${uuid}/submit`);
    }

    /**
     * Save the revision details during a revision phase
     *
     * @param requestBase the request to save
     */
    saveRequestRevision(requestBase: RequestBase): Observable<RequestBase> {
        let requestCopy: RequestBase = Object.assign({}, requestBase);
        return this.http.put<RequestBase>(`${this.resourceUrl}`, requestCopy);
    }

    submitRequestRevision(uuid: string): Observable<RequestBase> {
        return this.http.get<RequestBase>(`${this.resourceUrl}/${uuid}/submit`);
    }

    findByUuid(uuid: string): Observable<RequestDetail> {
        return this.http.get<RequestDetail>(`${this.resourceUrl}/${uuid}`);
    }

    getTemplateByUuid(uuid: string): Observable<RequestTemplate> {
        return this.http.get<RequestTemplate>(`${this.resourceUrl}/templates/${uuid}`);
    }

    deleteDraft(uuid: string): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/drafts/${uuid}`, {
            observe: 'response'
        });
    }

    /**
     * Process functions
     */
    validateRequest(uuid: string): Observable<HttpResponse<RequestBase>> {
        return this.http.get<RequestBase>(`${this.resourceUrl}/${uuid}/validate`, {
            observe: 'response'
        });
    }

    requestRevision(uuid: string, message: PodiumEventMessage)
        : Observable<HttpResponse<RequestBase>>
    {
        return this.http.post<RequestBase>(`${this.resourceUrl}/${uuid}/requestRevision`, message, {
            observe: 'response'
        });
    }

    approveRequest(uuid: string): Observable<HttpResponse<RequestBase>> {
        return this.http.get<RequestBase>(`${this.resourceUrl}/${uuid}/approve`, {
            observe: 'response'
        });
    }

    submitReview(uuid: string, reviewFeedback: RequestReviewFeedback) {
        let feedbackCopy: RequestReviewFeedback = Object.assign({}, reviewFeedback);
        return this.http.put<RequestBase>(`${this.resourceUrl}/${uuid}/review`, feedbackCopy, {
            observe: 'response'
        });
    }

    rejectRequest(uuid: string, message: PodiumEventMessage)
        : Observable<HttpResponse<RequestBase>>
    {
        return this.http.post<RequestBase>(`${this.resourceUrl}/${uuid}/reject`, message, {
            observe: 'response'
        });
    }

    startRequestDelivery(uuid: string): Observable<HttpResponse<RequestBase>> {
        return this.http.get<RequestBase>(`${this.resourceUrl}/${uuid}/startDelivery`, {
            observe: 'response'
        });
    }

    closeRequest(uuid: string, message?: PodiumEventMessage) {
        return this.http.post<RequestBase>(`${this.resourceUrl}/${uuid}/close`, message, {
            observe: 'response'
        });
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
