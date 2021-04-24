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
import { AccountService } from '../../core/auth/account.service';
import { Account } from '../../core/auth/account.model';
import { User } from '../../shared/user/user.model';

@Component({
    selector: 'pdm-settings',
    templateUrl: './settings.component.html'
})
export class SettingsComponent implements OnInit {

    error: string;
    success: string;
    settingsAccount: User;

    static copyAccount(account: Account): User {
        return { ...account };
    }

    constructor(
        private account: AccountService,
        private accountService: AccountService,
    ) {
    }

    ngOnInit () {
        this.accountService.identity().then((account) => {
            this.settingsAccount = SettingsComponent.copyAccount(account);
        });
    }

    save () {
        this.account.save(this.settingsAccount).subscribe(() => {
            this.error = null;
            this.success = 'OK';
            this.accountService.identity(true).then((account) => {
                this.settingsAccount = SettingsComponent.copyAccount(account);
            });
        }, () => {
            this.success = null;
            this.error = 'ERROR';
        });
    }

}
