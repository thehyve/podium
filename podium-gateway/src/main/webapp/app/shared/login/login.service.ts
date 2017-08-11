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
import { JhiLanguageService } from 'ng-jhipster';
import { Principal } from '../auth/principal.service';
import { AuthServerProvider } from '../auth/auth-jwt.service';
import { Router } from '@angular/router';

@Injectable()
export class LoginService {

    constructor (
        private languageService: JhiLanguageService,
        private principal: Principal,
        private authServerProvider: AuthServerProvider,
        private router: Router
    ) {}

    login (credentials, callback?) {
        let cb = callback || function() {};

        return new Promise((resolve, reject) => {
            this.authServerProvider.login(credentials).subscribe(data => {
                this.principal.identity(true).then(account => {
                    // After the login the language will be changed to
                    // the language selected by the user during his registration
                    if (account !== null) {
                        this.languageService.changeLanguage(account.langKey);
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
        this.principal.authenticate(null);
    }

    redirectUser() {
        Promise.all([
            this.principal.hasAuthority('ROLE_PODIUM_ADMIN'),
            this.principal.hasAuthority('ROLE_BBMRI_ADMIN'),
            this.principal.hasAuthority('ROLE_ORGANISATION_ADMIN'),
            this.principal.hasAuthority('ROLE_ORGANISATION_COORDINATOR'),
            this.principal.hasAuthority('ROLE_REVIEWER'),
            this.principal.hasAuthority('ROLE_RESEARCHER')
        ]).then(res => {
            if (res[0] || res[1]) {
                this.router.navigate(['/admin/user-management']);
            } else if (res[2]) {
                this.router.navigate(['/organisation/management']);
            } else if (res[3]) {
                this.router.navigate(['/requests/my-organisations']);
            } else if (res[4]) {
                this.router.navigate(['/requests/my-reviews']);
            } else if (res[5]) {
                this.router.navigate(['/requests/my-requests']);
            } else {
                this.router.navigate(['/']);
            }
        })
    }
}
