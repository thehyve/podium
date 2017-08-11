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
import { RedirectService } from '../shared/redirect/redirect.service';

@Component({
    selector: 'pdm-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: [
        'dashboard.scss'
    ]

})
export class DashboardComponent implements OnInit {

    constructor(
        private redirectService: RedirectService
    ) {

    }

    ngOnInit() {
        this.redirectService.navigateToLandingPageForRole();
    }
}
