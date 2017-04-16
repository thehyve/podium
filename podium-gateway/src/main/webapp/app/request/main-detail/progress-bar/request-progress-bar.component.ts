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
import { JhiLanguageService } from 'ng-jhipster';
import { RequestBase } from '../../../shared/request/request-base';
import { RequestStatus, REQUEST_STATUSES, REQUEST_STATUSES_MAP } from '../../../shared/request/request-status';

@Component({
    selector: 'pdm-request-progress-bar',
    templateUrl: './request-progress-bar.component.html',
    styleUrls: ['request-progress-bar.scss']
})

export class RequestProgressBarComponent implements OnInit {
    @Input() request: RequestBase;
    requestStatusOptions: ReadonlyArray<RequestStatus>;
    requestStatusMap: { [token: string]: RequestStatus; };

    constructor(
        private jhiLanguageService: JhiLanguageService
    ) {
        jhiLanguageService.setLocations(['request', 'requestStatus']);
        this.requestStatusOptions = REQUEST_STATUSES;
        this.requestStatusMap = REQUEST_STATUSES_MAP;
    }

    ngOnInit() {
        console.log('Opts ', this.requestStatusOptions);
        console.log('Opts ', this.requestStatusMap);
    }

    isActive(request: RequestBase, currentOrder: number): boolean {
        let reqStatus = request.status;
        let reqStatusOrder = this.requestStatusMap[reqStatus].order;

        return reqStatusOrder === currentOrder;
    }

    isCompleted(request: RequestBase, statusIndex: number): boolean {
        let reqStatus = request.status;
        let reqStatusOrder = this.requestStatusMap[reqStatus].order;

        return reqStatusOrder > statusIndex;
    }
}
