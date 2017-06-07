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
import { ReviewRound } from '../review-round';
import { RequestReviewFeedback } from '../request-review-feedback';
import { RequestReviewDecision } from '../request-review-decision';
import { RequestService } from '../request.service';
import { RequestBase } from '../request-base';
import { Subscription } from 'rxjs';

@Component({
    selector: 'pdm-request-review-panel',
    templateUrl: './request-review-panel.component.html'
})

export class RequestReviewPanelComponent implements OnInit, OnDestroy {

    lastReviewFeedback: RequestReviewFeedback[];
    requestSubscription: Subscription;

    @Input()
    reviewRounds: ReviewRound[];

    private optionStyles = [
        {style: 'tag-success', advise: RequestReviewDecision.Approved},
        {style: 'tag-danger', advise: RequestReviewDecision.Rejected},
        {style: 'tag-default', advise: RequestReviewDecision.None},
    ];

    constructor(
        private requestService: RequestService
    ) {
        this.requestSubscription = this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.reviewRounds = request.reviewRounds;
            this.lastReviewFeedback = this.requestService.getLastReviewFeedbacks(this.reviewRounds);
        });
    }

    toggleAdviseStyle(advise: RequestReviewDecision): string {
        let foundStyle = this.optionStyles.find((optionStyle) => {
            return optionStyle.advise === advise;
        });
        return foundStyle ? foundStyle.style : 'tag-default';
    }

    ngOnInit(): void {
        if (this.reviewRounds.length) {
            this.lastReviewFeedback = this.requestService.getLastReviewFeedbacks(this.reviewRounds);
        }
    }

    ngOnDestroy() {
        if (this.requestSubscription) {
            this.requestSubscription.unsubscribe();
        }
    }
}

