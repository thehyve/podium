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
import { Principal } from '../shared';
import { User } from '../shared/user/user.model';
import { JhiLanguageService } from 'ng-jhipster';

@Component({
    selector: 'dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: [
        'dashboard.scss'
    ]

})
export class DashboardComponent implements OnInit {
    user: User;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private principal: Principal
    ) {
        this.jhiLanguageService.setLocations(['dashboard']);
    }

    ngOnInit() {
        this.principal.identity().then((account) => {
            this.user = account;
        });
    }
}
