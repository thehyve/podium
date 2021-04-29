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
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, ReplaySubject, of } from 'rxjs';
import { shareReplay, tap, catchError } from 'rxjs/operators';

import { StateStorageService } from './state-storage.service';
import { ApplicationConfigService } from '../config/application-config.service';
import { Account } from './account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
    private userIdentity: Account | null = null;
    private authenticationState = new ReplaySubject<Account | null>(1);
    private accountCache$?: Observable<Account | null>;

    constructor(
        private config: ApplicationConfigService,
        private http: HttpClient,
        private stateStorageService: StateStorageService,
        private router: Router,
    ) {}

    authenticate(identity: Account | null): void {
        this.userIdentity = identity;
        this.authenticationState.next(this.userIdentity);
    }

    hasAnyAuthority(authorities: string[] | string): boolean {
        if (!this.userIdentity || !this.userIdentity.authorities) {
            return false;
        }
        let authorityList = [authorities].flat();
        return this.userIdentity.authorities.some((e) => authorityList.includes(e));
    }

    hasAuthority (authority: string): Promise<boolean> {
        if (!this.userIdentity) {
           return Promise.resolve(false);
        }

        return this.identity().toPromise().then(id => {
            return Promise.resolve(id.authorities && id.authorities.indexOf(authority) !== -1);
        }, () => {
            return Promise.resolve(false);
        });
    }

    identity(force?: boolean): Observable<Account | null> {
        if (!this.accountCache$ || force || !this.isAuthenticated()) {
            this.accountCache$ = this.fetch().pipe(
                catchError(() => of(null)),
                tap((account: Account | null) => {
                    this.authenticate(account);
                    if (account) {
                        this.navigateToStoredUrl();
                    }
                }),
                shareReplay()
            );
        }
        return this.accountCache$;
    }

    isAuthenticated (): boolean {
        return this.userIdentity !== null;
    }

    getAuthenticationState(): ReplaySubject<any> {
        return this.authenticationState;
    }

    isIdentityResolved (): boolean {
        return this.userIdentity !== undefined;
    }

    getImageUrl(): string {
        return this.isIdentityResolved () ? this.userIdentity.imageUrl : null;
    }

    private fetch(): Observable<Account> {
        let url = this.config.getUaaEndpoint('api/account');
        return this.http.get<Account>(url);
    }

    private navigateToStoredUrl(): void {
        // previousState can be set in the authExpiredInterceptor and in the userRouteAccessService
        // if login is successful, go to stored previousState and clear previousState
        const previousUrl = this.stateStorageService.getUrl();
        if (previousUrl) {
            this.stateStorageService.clearUrl();
            this.router.navigateByUrl(previousUrl);
        }
    }

    save(account: any): Observable<any> {
        let url = this.config.getUaaEndpoint('api/account');
        return this.http.post(url, account);
    }
}
