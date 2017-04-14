/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, ViewChild } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';

@Component({
    selector: 'pdm-request-action-toolbar',
    templateUrl: './request-action-toolbar.component.html',
    styleUrls: ['request-action-toolbar.scss']
})

export class RequestActionToolbarComponent implements OnInit {

    constructor(private jhiLanguageService: JhiLanguageService) {
        this.jhiLanguageService.setLocations(['request']);
    }

    ngOnInit() {

    }

    isInitialRequest(): boolean {
        return true;
    }

    isValidation(): boolean {
        return true;
    }

    isReview(): boolean {
        return true;
    }

}
