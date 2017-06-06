/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import { RequestService } from '../request';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PodiumEventMessage } from '../event/podium-event-message';
import { RequestReviewDecision } from '../request/request-review-decision';
import { RequestUpdateDialogComponent } from './request-update-dialog.component';

@Component({
    templateUrl: './request-update-dialog.component.html',
    styleUrls: ['request-update-dialog.scss']
})

export class RequestUpdateReviewDialogComponent extends RequestUpdateDialogComponent implements OnInit {
    reviewStatus: RequestReviewDecision;
    headerStyle: string;
    buttonStyle: string;
    status: string;
    public message: PodiumEventMessage = new PodiumEventMessage();

    constructor(protected jhiLanguageService: JhiLanguageService,
                protected requestService: RequestService,
                protected activeModal: NgbActiveModal) {
        super(jhiLanguageService, requestService, activeModal);
    }

    ngOnInit() {
        this.applyStyles();
    }

    applyStyles() {
        this.status = RequestReviewDecision[this.reviewStatus];
        if (this.status === RequestReviewDecision[RequestReviewDecision.Rejected]) {
            this.headerStyle = 'reject-header';
            this.buttonStyle = 'btn-danger';
        } else {
            this.headerStyle = 'approve-header';
            this.buttonStyle = 'btn-success';
        }
    }

    close() {
        super.close();
    }

    /**
     * Confirm and submit a status update with a message
     * returns an unsubscribed observable with the action
     */
    confirmStatusUpdate() {
        if (this.reviewStatus === RequestReviewDecision.Rejected) {
            // TODO: submit to review api
            this.requestService.submitReview(this.request.uuid, this.message)
                .subscribe((res) => this.onSuccess(res));
        }

        if (this.reviewStatus === RequestReviewDecision.Approved) {
            // TODO: submit to review api
            this.requestService.submitReview(this.request.uuid, this.message)
                .subscribe((res) => this.onSuccess(res));
        }

        super.onUnknownStatus();
    }
}
