/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager } from '../core/util/event-manager.service';
import { Account } from '../core/auth/account.model';
import { AccountService } from '../core/auth/account.service';
import { RedirectService } from '../core/auth/redirect.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'pdm-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.component.scss'
    ]
})
export class PdmHomeComponent implements OnInit, OnDestroy {
    account: Account;
    modalRef: NgbModalRef;
    authenticationSuccessEvents: Subscription;

    constructor(
        private accountService: AccountService,
        private eventManager: EventManager,
        private redirectService: RedirectService,
    ) {

    }

    ngOnInit() {
        this.accountService.identity().subscribe((account) => {
            this.account = account;
            if (this.isAuthenticated()) {
                this.redirectService.redirectUser();
            }
        });
        this.registerAuthenticationSuccess();
    }

    ngOnDestroy() {
        this.authenticationSuccessEvents.unsubscribe();
    }

    registerAuthenticationSuccess() {
        this.authenticationSuccessEvents = this.eventManager.subscribe('authenticationSuccess', (message) => {
            if (this.isAuthenticated()) {
                this.redirectService.redirectUser();
            }
            this.accountService.identity().subscribe((account) => {
                this.account = account;
            });
        });
    }

    isAuthenticated() {
        return this.accountService.isAuthenticated();
    }
}
