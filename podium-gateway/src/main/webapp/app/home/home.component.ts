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
import { JhiEventManager } from 'ng-jhipster';
import { Account, AccountService } from '../shared';
import { RedirectService } from '../shared/auth/redirect.service';

@Component({
    selector: 'pdm-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.scss'
    ]

})
export class PdmHomeComponent implements OnInit {
    account: Account;
    modalRef: NgbModalRef;

    constructor(
        private accountService: AccountService,
        private eventManager: JhiEventManager,
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
