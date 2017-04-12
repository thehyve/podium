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
import { RequestDetail } from '../../../shared/request/request-detail';

@Component({
    selector: 'pdm-request-detail',
    templateUrl: './request-detail.component.html',
    styleUrls: ['request-detail.scss']
})

export class RequestDetailComponent {

    public requestDetail: RequestDetail;

    constructor() {

    }

    setRequestDetail(requestDetail) {
        this.requestDetail = requestDetail;
    }
}
