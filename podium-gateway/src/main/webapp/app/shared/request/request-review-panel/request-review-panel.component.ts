/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, Input } from '@angular/core';
import { ReviewRound } from '../review-round';
import { RequestReviewFeedback } from '../request-review-feedback';

@Component({
    selector: 'pdm-request-review-panel',
    templateUrl: './request-review-panel.component.html'
})

export class RequestReviewPanelComponent implements OnInit {

    lastReviewFeedback: RequestReviewFeedback[];

    @Input()
    reviewRounds: ReviewRound[];

    constructor() {

    }

    private getLastReviewFeedback() {
        // get the latest start date of review rounds
        let _lastReviewRoundDate = Math.max.apply(Math, this.reviewRounds.map((reviewRound) => {
            return reviewRound.startDate;
        }));

        // get the latest round
        let _lastReviewRound = this.reviewRounds.find((reviewRound) => {
            return reviewRound.startDate.getTime() === _lastReviewRoundDate;
        });

        // return feedback of last review round
        return _lastReviewRound.reviewFeedback;
    }

    ngOnInit(): void {
        if (this.reviewRounds.length) {
            this.lastReviewFeedback = this.getLastReviewFeedback();
        }
    }
}

