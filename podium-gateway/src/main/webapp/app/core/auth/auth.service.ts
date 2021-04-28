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
import { StateStorageService } from './state-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {

    constructor(
        private accountService: AccountService,
        private stateStorageService: StateStorageService,
        private router: Router
    ) {}

    authorize (force): Promise<boolean> {
        let self = this;
        let authReturn = self.accountService.identity(force).toPromise().then(authThen);

        return authReturn;

        function authThen () {
            let isAuthenticated = self.accountService.isAuthenticated();
            let toStateInfo = self.stateStorageService.getDestinationState().destination;

            // an authenticated user can't access to login and register pages
            if (isAuthenticated && (toStateInfo.name === 'register')) {
                self.router.navigate(['']);
                return false;
            }

            // recover and clear previousState after external login redirect (e.g. oauth2)
            let fromStateInfo = self.stateStorageService.getDestinationState().from;
            let previousState = self.stateStorageService.getPreviousState();
            if (isAuthenticated && !fromStateInfo.name && previousState) {
                self.stateStorageService.resetPreviousState();
                self.router.navigate([previousState.name], { queryParams:  previousState.params  });
                return false;
            }

            if (toStateInfo.data.authorities && toStateInfo.data.authorities.length > 0) {
                let hasAnyAuthority = self.accountService.hasAnyAuthority(toStateInfo.data.authorities);
                if (!hasAnyAuthority) {
                    if (isAuthenticated) {
                        // user is signed in but not authorized for desired state
                        self.router.navigate(['accessdenied']);
                    } else {
                        // user is not authenticated. Show the state they wanted before you
                        // send them to the login service, so you can return them when you're done
                        let toStateParamsInfo = self.stateStorageService.getDestinationState().params;
                        self.stateStorageService.storePreviousState(toStateInfo.name, toStateParamsInfo);
                        // now, send them to the signin state so they can log in
                        self.router.navigate(['accessdenied']).then(() => {
                            self.router.navigate(['/']);
                        });
                    }
                }
                return hasAnyAuthority;
            }

            return true;
        }
    }
}
