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

import { LoginModalService } from '../login/login-modal.service';
import { Principal } from './principal.service';
import { StateStorageService } from './state-storage.service';

@Injectable()
export class AuthService {

    constructor(
        private principal: Principal,
        private stateStorageService: StateStorageService,
        private loginModalService: LoginModalService,
        private router: Router
    ) {}

    authorize (force) {
        let authReturn = this.principal.identity(force).then(authThen.bind(this));

        return authReturn;

        function authThen () {
            let isAuthenticated = this.principal.isAuthenticated();
            let toStateInfo = this.stateStorageService.getDestinationState().destination;
            let canActivate = true;

            // an authenticated user can't access to login and register pages
            if (isAuthenticated && (toStateInfo.name === 'register')) {
                this.router.navigate(['']);
                canActivate = false;
            }

            // recover and clear previousState after external login redirect (e.g. oauth2)
            let fromStateInfo = this.stateStorageService.getDestinationState().from;
            let previousState = this.stateStorageService.getPreviousState();
            if (isAuthenticated && !fromStateInfo.name && previousState) {
                this.stateStorageService.resetPreviousState();
                this.router.navigate([previousState.name], { queryParams:  previousState.params  });
                canActivate = false;
            }

            if (toStateInfo.data.authorities && toStateInfo.data.authorities.length > 0 &&
                !this.principal.hasAnyAuthority(toStateInfo.data.authorities)) {

                if (isAuthenticated) {
                    // user is signed in but not authorized for desired state
                    this.router.navigate(['accessdenied']);
                    canActivate = false;
                } else {
                    canActivate = false;
                    // user is not authenticated. Show the state they wanted before you
                    // send them to the login service, so you can return them when you're done
                    let toStateParamsInfo = this.stateStorageService.getDestinationState().params;
                    this.stateStorageService.storePreviousState(toStateInfo.name, toStateParamsInfo);
                    // now, send them to the signin state so they can log in
                    this.router.navigate(['accessdenied']).then(() => {
                        this.loginModalService.open();
                    });
                }
            }
            return canActivate;
        }
    }
}
