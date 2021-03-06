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
import { AccountService } from '../core/auth/account.service';
import { User } from '../shared/user/user.model';

@Component({
    selector: 'pdm-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: [
        'dashboard.scss'
    ]

})
export class DashboardComponent implements OnInit {
    user: User;

    constructor(
        private accountServie: AccountService
    ) {

    }

    ngOnInit() {
        this.accountServie.identity().subscribe((account) => {
            this.user = account;
        });
    }
}
