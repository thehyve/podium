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
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager } from '../core/util/event-manager.service';
import { Account } from '../core/auth/account.model';
import { AccountService } from '../core/auth/account.service';
import { RedirectService } from '../core/auth/redirect.service';

@Component({
    selector: 'pdm-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.component.scss'
    ]

})
export class PdmHomeComponent implements OnInit {
    account: Account;
    modalRef: NgbModalRef;

    constructor(
        private accountService: AccountService,
        private eventManager: EventManager,
        private redirectService: RedirectService,
    ) {

    }

    ngOnInit() {
        this.accountService.identity().then((account) => {
            this.account = account;
        });
        this.registerAuthenticationSuccess();

        if (this.isAuthenticated()) {
            this.redirectService.redirectUser();
        }
    }

    registerAuthenticationSuccess() {
        this.eventManager.subscribe('authenticationSuccess', (message) => {
            this.accountService.identity().then((account) => {
                this.account = account;
            });
        });
    }

    isAuthenticated() {
        return this.accountService.isAuthenticated();
    }
}
