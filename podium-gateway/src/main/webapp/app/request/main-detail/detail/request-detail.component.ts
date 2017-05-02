/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component } from '@angular/core';
import { RequestDetail } from '../../../shared/request/request-detail';
import { RequestBase } from '../../../shared/request/request-base';

@Component({
    selector: 'pdm-request-detail',
    templateUrl: './request-detail.component.html'
})

export class RequestDetailComponent {

    public request: RequestBase;
    public requestDetail: RequestDetail;

    constructor() {

    }

    setRequest(request) {
        this.request = request;
        this.requestDetail = request.requestDetail;
    }
}
