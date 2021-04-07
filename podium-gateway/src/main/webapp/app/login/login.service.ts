/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AccountService } from '../core/auth/account.service';
import { AuthServerProvider } from '../core/auth/auth-jwt.service';

@Injectable({ providedIn: 'root' })
export class LoginService {

    constructor (
        private translateService: TranslateService,
        private accountService: AccountService,
        private authServerProvider: AuthServerProvider
    ) {}

    login (credentials, callback?) {
        let cb = callback || function() {};

        return new Promise((resolve, reject) => {
            this.authServerProvider.login(credentials).subscribe(data => {
                this.accountService.identity(true).then(account => {
                    // After the login the language will be changed to
                    // the language selected by the user during his registration
                    if (account !== null) {
                        this.translateService.currentLang = account.langKey;
                    }
                    resolve(data);
                });
                return cb();
            }, err => {
                this.logout();
                reject(err);
                return cb(err);
            });
        });
    }

    logout () {
        this.authServerProvider.logout().subscribe();
        this.accountService.authenticate(null);
    }
}
