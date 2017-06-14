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
import { RequestReviewFeedback } from '../request/request-review-feedback';
import { User } from '../user/user.model';

@Component({
    templateUrl: './request-update-dialog.component.html',
    styleUrls: ['request-update-dialog.scss']
})

export class RequestUpdateReviewDialogComponent extends RequestUpdateDialogComponent implements OnInit {
    reviewStatus: RequestReviewDecision;
    currentUser: User;
    status: string;
    panelStyles: any;
    public message: PodiumEventMessage = new PodiumEventMessage();

    constructor(protected jhiLanguageService: JhiLanguageService,
                protected requestService: RequestService,
                protected activeModal: NgbActiveModal) {
        super(jhiLanguageService, requestService, activeModal);
    }

    ngOnInit() {
        this.status = RequestReviewDecision[this.reviewStatus];
        this.panelStyles = this.applyStyles(RequestReviewDecision[this.reviewStatus]);
    }

    close() {
        super.close();
    }

    composeReviewFeedback(): RequestReviewFeedback {
        let feedback = this.requestService.getLastReviewFeedbackByUser(this.request, this.currentUser);
        if (feedback) {
            feedback.advice = this.reviewStatus;
            feedback.message = this.message;
        }
        return feedback;
    }

    /**
     * Confirm and submit a status update with a message
     * returns an unsubscribed observable with the action
     */
    confirmStatusUpdate() {
        this.requestService.submitReview(this.request.uuid, this.composeReviewFeedback())
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onUnknownStatus()
            );
    }
}
