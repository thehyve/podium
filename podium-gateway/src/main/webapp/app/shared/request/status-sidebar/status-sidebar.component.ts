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
    selector: 'pdm-request-status-sidebar',
    templateUrl: './status-sidebar.component.html',
    styleUrls: ['status-sidebar.scss']
})

export class RequestStatusSidebarComponent implements OnInit {

    constructor(private jhiLanguageService: JhiLanguageService) {
        this.jhiLanguageService.setLocations(['request', 'requestStatus']);
    }

    ngOnInit() {

    }
}
