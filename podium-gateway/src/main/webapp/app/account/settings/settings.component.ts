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
import { AccountService, JhiLanguageHelper } from '../../shared';
import { Account } from '../../shared/user/account.model';
import { User } from '../../shared/user/user.model';

@Component({
    selector: 'pdm-settings',
    templateUrl: './settings.component.html'
})
export class SettingsComponent implements OnInit {

    error: string;
    success: string;
    settingsAccount: User;
    languages: any[];

    static copyAccount(account: Account): User {
        return { ...account };
    }

    constructor(
        private account: AccountService,
        private accountService: AccountService,
        private languageService: JhiLanguageService,
        private languageHelper: JhiLanguageHelper
    ) {
    }

    ngOnInit () {
        this.accountService.identity().then((account) => {
            this.settingsAccount = SettingsComponent.copyAccount(account);
        });
        this.languageHelper.getAll().then((languages) => {
            this.languages = languages;
        });
    }

    save () {
        this.account.save(this.settingsAccount).subscribe(() => {
            this.error = null;
            this.success = 'OK';
            this.accountService.identity(true).then((account) => {
                this.settingsAccount = SettingsComponent.copyAccount(account);
            });
            this.languageService.getCurrent().then((current) => {
                if (this.settingsAccount.langKey !== current) {
                    this.languageService.changeLanguage(this.settingsAccount.langKey);
                }
            });
        }, () => {
            this.success = null;
            this.error = 'ERROR';
        });
    }

}
