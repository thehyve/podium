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
import { JhiLanguageService } from 'ng-jhipster';

@Component({
    selector: 'pdm-not-found',
    templateUrl: './not-found.component.html'
})
export class NotFoundComponent implements OnInit {

    constructor(
        private jhiLanguageService: JhiLanguageService
    ) {

    }

    ngOnInit() {
    }
}
