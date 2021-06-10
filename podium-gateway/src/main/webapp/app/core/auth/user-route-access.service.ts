/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Injectable, isDevMode } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { AccountService } from './account.service';
import { StateStorageService } from './state-storage.service';

@Injectable({ providedIn: 'root' })
export class UserRouteAccessService implements CanActivate {
    constructor(
        private router: Router,
        private accountService: AccountService,
        private stateStorageService: StateStorageService
    ) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
        return this.accountService.identity().pipe(
            map(account => {
                const authorities = route.data['authorities'];

                if (!authorities || authorities.length === 0 || this.accountService.hasAnyAuthority(authorities)) {
                    if (route.data['rememberPage'] !== false) {
                        this.stateStorageService.storeUrl(state.url);
                    }
                    return true;
                }

                if (account) {
                    if (isDevMode()) {
                        console.error('User has not any of required authorities: ', authorities);
                    }
                    this.router.navigate(['accessdenied']);
                    return false;
                }

                if (route.data['rememberPage'] !== false) {
                    this.stateStorageService.storeUrl(state.url);
                }
                this.router.navigate(['/']);
                return false;
            })
        );
    }
}
