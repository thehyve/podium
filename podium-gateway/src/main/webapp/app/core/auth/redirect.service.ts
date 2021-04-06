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
import { Router } from '@angular/router';
import { AccountService } from './account.service';

@Injectable()
export class RedirectService {

    constructor (
        private accountService: AccountService,
        private router: Router
    ) {}


    redirectUser() {
        Promise.all([
            this.accountService.hasAuthority('ROLE_PODIUM_ADMIN'),
            this.accountService.hasAuthority('ROLE_BBMRI_ADMIN'),
            this.accountService.hasAuthority('ROLE_ORGANISATION_ADMIN'),
            this.accountService.hasAuthority('ROLE_ORGANISATION_COORDINATOR'),
            this.accountService.hasAuthority('ROLE_REVIEWER'),
            this.accountService.hasAuthority('ROLE_RESEARCHER')
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
        });
    }
}
