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
import { RequestService } from '../request/request.service';
import { RequestBase } from '../request/request-base';
import { PodiumEvent } from './podium-event';
import { RequestAccessService } from '../request/request-access.service';
import { RequestStatusOptions } from '../request/request-status/request-status.constants';
import { Subscription } from 'rxjs';
import { RequestUpdateAction } from '../status-update/request-update-action';

@Component({
    selector: 'pdm-event-message-component',
    templateUrl: 'podium-event-message.component.html',
    styleUrls: ['podium-event-message.component.scss']
})

export class PodiumEventMessageComponent implements OnInit, OnDestroy {
    @Input()
    request: RequestBase;
    lastEvent: PodiumEvent;
    requestSubscription: Subscription;

    constructor(
        private requestService: RequestService,
        private requestAccessService: RequestAccessService
    ) {
    }

    ngOnInit() {
        this.requestSubscription = this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
            this.findLastHistoricReviewMessageEventForCurrentStatus();
        });

        this.findLastHistoricReviewMessageEventForCurrentStatus();
    }

    ngOnDestroy() {
        if (this.requestSubscription) {
            this.requestSubscription.unsubscribe();
        }
    }

    findLastHistoricReviewMessageEventForCurrentStatus() {
        let messageEvents = this.request.historicEvents.filter((event) => {
            return event.data.messageSummary != null
                && event.data.targetStatus === this.request.requestReview.status.toLocaleString();
        });

        this.lastEvent = messageEvents[messageEvents.length - 1];
    }

    isRevisionEvent(): boolean {
        if (!this.lastEvent) {
            return false;
        }
        let revisionAction = RequestUpdateAction.Revision;
        return this.lastEvent.data.targetStatus === revisionAction.toLocaleString();
    }

    isRejectionEvent(): boolean {
        if (!this.lastEvent) {
            return false;
        }
        let closedAction = RequestStatusOptions.Closed;
        return this.lastEvent.data.targetStatus === closedAction.toLocaleString();
    }

    isRequestOwner(): boolean {
        return this.requestAccessService.isRequesterOf(this.request);
    }

}
