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
import { RequestService } from '../request/request.service';
import { RequestBase } from '../request/request-base';
import { PodiumEvent } from './podium-event';
import { RequestStatusUpdateAction } from '../status-update/request-status-update-action';
import { RequestAccessService } from '../request/request-access.service';
import { RequestStatusOptions } from '../request/request-status/request-status.constants';

@Component({
    selector: 'pdm-event-message-component',
    templateUrl: 'podium-event-message.component.html',
    styleUrls: ['podium-event-message.component.scss']
})

export class PodiumEventMessageComponent implements OnInit {
    @Input()
    request: RequestBase;
    lastEvent: PodiumEvent;

    constructor(
        private requestService: RequestService,
        private requestAccessService: RequestAccessService
    ) {

    }

    ngOnInit() {
        this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
            this.findLastHistoricMessageEventForCurrentStatus();
        });

        this.findLastHistoricMessageEventForCurrentStatus();
    }

    findLastHistoricMessageEventForCurrentStatus() {
        let messageEvents = this.request.historicEvents.filter((event) => {
            return event.data.messageSummary != null;
        });

        this.lastEvent = messageEvents[messageEvents.length - 1];
    }

    isRevisionEvent(): boolean {
        let revisionAction = RequestStatusUpdateAction.Revision;
        let revisionStatus = RequestStatusUpdateAction[revisionAction];
        return this.lastEvent.data.targetStatus === revisionStatus;
    }

    isRejectionEvent(): boolean {
        let closedAction = RequestStatusOptions.Closed;
        let closedStatus = RequestStatusOptions[closedAction];
        return this.lastEvent.data.targetStatus === closedStatus;
    }

    isRequestOwner(): boolean {
        return this.requestAccessService.isRequesterOf(this.request);
    }

}
