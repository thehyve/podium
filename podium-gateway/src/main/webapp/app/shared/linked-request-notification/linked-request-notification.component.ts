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
import { RequestBase } from '../request/request-base';
import { RequestAccessService } from '../request/request-access.service';
import { RequestService } from '../request/request.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'pdm-linked-notification',
    templateUrl: 'linked-request-notification.component.html',
    styleUrls: ['linked-request-notification.component.scss']
})

export class LinkedRequestNotificationComponent implements OnInit, OnDestroy {

    @Input() request: RequestBase;
    requestSubscription: Subscription;

    constructor(
        private requestService: RequestService,
        private requestAccessService: RequestAccessService
    ) {}

    ngOnInit() {
        this.requestSubscription = this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
        });
    }

    isRequester() {
        return this.requestAccessService.isRequesterOf(this.request);
    }

    isOrganisationCoordinator() {
        return this.requestAccessService.isCoordinatorFor(this.request);
    }

    isReviewer() {
        return this.requestAccessService.isReviewerFor(this.request);
    }

    ngOnDestroy() {
        if (this.requestSubscription) {
            this.requestSubscription.unsubscribe();
        }
    }

}
