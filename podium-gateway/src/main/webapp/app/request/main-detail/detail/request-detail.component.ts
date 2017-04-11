/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'pdm-request-detail',
    templateUrl: './request-detail.component.html',
    styleUrls: ['request-detail.scss']
})

export class RequestDetailComponent implements OnInit {

    error: any;
    success: any;

    constructor() {

    }

    ngOnInit(): void {
    }

    private onSuccess(result) {
        this.error =  null;
        this.success = 'SUCCESS';
    }

    private onError(error) {
        this.error =  'ERROR';
        this.success = null;
    }

}
