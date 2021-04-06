/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { RequestReviewFeedback } from '../request-review-feedback';
import { RequestReviewDecision } from '../request-review-decision';
import { RequestService } from '../request.service';
import { RequestBase } from '../request-base';
import { Subscription } from 'rxjs';
import { RequestAccessService } from '../request-access.service';
import { AccountService } from '../../auth/account.service';

@Component({
    selector: 'pdm-request-review-panel',
    templateUrl: './request-review-panel.component.html'
})

export class RequestReviewPanelComponent implements OnInit, OnDestroy {

    lastReviewFeedback: RequestReviewFeedback[];
    requestSubscription: Subscription;

    @Input()
    request: RequestBase;

    private optionStyles = [
        {style: 'badge-success', advise: RequestReviewDecision.Approved},
        {style: 'badge-danger', advise: RequestReviewDecision.Rejected},
        {style: 'badge-default', advise: RequestReviewDecision.None},
    ];

    constructor(
        private requestService: RequestService,
        private accountService: AccountService,
        private requestAccessService: RequestAccessService
    ) {
        this.requestSubscription = this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
            this.setRequestReviewFeedback();
        });
    }

    toggleAdviseStyle(advise: RequestReviewDecision): string {
        let foundStyle = this.optionStyles.find((optionStyle) => {
            return optionStyle.advise === advise;
        });
        return foundStyle ? foundStyle.style : 'badge-default';
    }

    ngOnInit(): void {
        this.setRequestReviewFeedback();
    }

    ngOnDestroy() {
        if (this.requestSubscription) {
            this.requestSubscription.unsubscribe();
        }
    }

    hasLastReviewFeedback(): boolean {
        return this.lastReviewFeedback && this.lastReviewFeedback[0] !== undefined;
    }

    setRequestReviewFeedback() {
        if (this.request.reviewRound) {
            if (this.requestAccessService.isReviewerFor(this.request)) {
                this.accountService.identity().then((account) => {
                    this.lastReviewFeedback = [
                        this.requestService.getLastReviewFeedbackByUser(this.request, account)
                    ];
                });
            } else {
                this.lastReviewFeedback = this.request.reviewRound.reviewFeedback;
            }
        }
    }
}

