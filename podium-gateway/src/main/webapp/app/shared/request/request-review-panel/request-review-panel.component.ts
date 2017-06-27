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
import { Principal } from '../../auth/principal.service';

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
        {style: 'tag-success', advise: RequestReviewDecision.Approved},
        {style: 'tag-danger', advise: RequestReviewDecision.Rejected},
        {style: 'tag-default', advise: RequestReviewDecision.None},
    ];

    constructor(
        private requestService: RequestService,
        private principal: Principal,
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
        return foundStyle ? foundStyle.style : 'tag-default';
    }

    ngOnInit(): void {
        this.setRequestReviewFeedback();
    }

    ngOnDestroy() {
        if (this.requestSubscription) {
            this.requestSubscription.unsubscribe();
        }
    }

    setRequestReviewFeedback() {
        if (this.request.reviewRounds.length) {
            if (this.requestAccessService.isReviewerFor(this.request)) {
                this.principal.identity().then((account) => {
                    this.lastReviewFeedback = [
                        this.requestService.getLastReviewFeedbackByUser(this.request, account)
                    ];
                });
            } else {
                this.lastReviewFeedback = this.requestService.getLastReviewFeedbacks(this.request.reviewRounds);
            }
        }
    }
}

