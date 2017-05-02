/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component } from '@angular/core';
import { RequestDetail } from '../../../shared/request/request-detail';
import { RequestBase } from '../../../shared/request/request-base';
import { RequestService } from '../../../shared/request/request.service';
import { RequestReviewFeedback } from '../../../shared/request/request-review-feedback';

@Component({
    selector: 'pdm-request-detail',
    templateUrl: './request-detail.component.html'
})

export class RequestDetailComponent {

    public request: RequestBase;
    public requestDetail: RequestDetail;

    constructor(
        private requestService: RequestService
    ) {

    }

    setRequest(request) {
        this.request = request;
        this.requestDetail = request.requestDetail;
    }

    cancel() {

    }

    rejectRequest() {
        this.requestService.rejectRequest(this.request.uuid)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    saveRequest() {
        this.requestService.saveRequest(this.request)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    submitRequest() {
        // Save the request
        this.requestService.saveRequest(this.request)
            // Submit the request
            .flatMap(() => this.requestService.submitRequest(this.request.uuid))
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    submitReview(requestReviewFeedback: RequestReviewFeedback) {
        this.requestService.submitReview(this.request.uuid, requestReviewFeedback)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    requireRequestRevision() {
        this.requestService.requireRevision(this.request.uuid)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    validateRequest() {
        this.requestService.validateRequest(this.request.uuid)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    approveRequest() {
        this.requestService.approveRequest(this.request.uuid)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    onSuccess(requestBase: RequestBase) {
        console.log('success ', requestBase);
        this.request = requestBase;
    }

    onError(err) {
        console.log('error ', err);
    }

}
