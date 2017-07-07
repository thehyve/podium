/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Injectable, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { OrganisationAuthorityOptions, PodiumAuthorityOptions } from '../authority/authority.constants';
import { Subscription } from 'rxjs';
import { User } from '../user/user.model';
import { Principal } from '../auth/principal.service';

@Injectable()
export class RedirectService implements OnDestroy {

    authenticationSubscription: Subscription;
    currentUser: User;

    constructor(
        private router: Router,
        private principal: Principal
    ) {
        this.authenticationSubscription = this.principal.getAuthenticationState().subscribe(
            (identity: User) => {
                this.currentUser = identity;
            }
        );
    }

    ngOnDestroy() {
        if (this.authenticationSubscription) {
            this.authenticationSubscription.unsubscribe();
        }
    }

    navigateToLandingPageForRole() {
        let auth = this.currentUser.authorities;
        let path;

        if (auth.indexOf(PodiumAuthorityOptions.ROLE_PODIUM_ADMIN) > -1) {
            path = '/admin/user-management';
        } else if (auth.indexOf(PodiumAuthorityOptions.ROLE_BBMRI_ADMIN) > -1) {
            path = '/bbmri/user-management';
        } else if (auth.indexOf(OrganisationAuthorityOptions.ROLE_ORGANISATION_ADMIN) > -1) {
            path = '/organisation/user-management';
        } else if (auth.indexOf(OrganisationAuthorityOptions.ROLE_ORGANISATION_COORDINATOR) > -1) {
            path = '/requests/my-organisations';
        } else if (auth.indexOf(OrganisationAuthorityOptions.ROLE_REVIEWER) > -1) {
            path = '/requests/my-reviews';
        } else if (auth.indexOf(PodiumAuthorityOptions.ROLE_RESEARCHER) > -1) {
            path = '/requests/my-requests';
        } else {
            console.error('Cannot match AuthorityOption in: ', auth);
        }
        this.router.navigate([path]);
    }
}
