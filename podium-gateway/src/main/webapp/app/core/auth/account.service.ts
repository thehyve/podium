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
import { BehaviorSubject, Observable, of, map } from 'rxjs';
import { shareReplay, tap, catchError } from 'rxjs/operators';
import { ApplicationConfigService } from '../config/application-config.service';
import { Account } from './account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
    private userIdentity: Account | null = null;
    private authenticationState = new BehaviorSubject<any>(null);
    private accountCache$?: Observable<Account | null>;

    constructor (
        private config: ApplicationConfigService,
        private http: HttpClient,
    ) {}

    authenticate(_identity: Account | null) {
        this.userIdentity = _identity;
        this.authenticationState.next(this.userIdentity);
    }

    hasAnyAuthority(authorities: string[]): boolean {
        if (!this.userIdentity || !this.userIdentity.authorities) {
            return false;
        }
        return this.userIdentity.authorities.some((authority: string) => authorities.includes(authority));
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
        if (force === true) {
            this.userIdentity = undefined;
        }

        // check and see if we have retrieved the _identity data from the server.
        // if we have, reuse it by immediately resolving
        if (this.userIdentity) {
            return of(this.userIdentity);
        }

        // retrieve the _identity data from the server, update the _identity object, and then resolve.
        this.accountCache$ = this.get().pipe(
            map(account => {
                this.authenticate(account);
                return this.userIdentity;
            }),
            catchError(() => {
                this.authenticate(null);;
                return null;
            }),
            shareReplay()
        );
        return this.accountCache$;
    }

    isAuthenticated (): boolean {
        return this.userIdentity !== null;
    }

    getAuthenticationState(): BehaviorSubject<any> {
        return this.authenticationState;
    }

    isIdentityResolved (): boolean {
        return this.userIdentity !== undefined;
    }

    getImageUrl(): String {
        return this.isIdentityResolved () ? this.userIdentity.imageUrl : null;
    }

    private get(): Observable<Account> {
        let url = this.config.getUaaEndpoint('api/account');
        return this.http.get<Account>(url);
    }

    save(account: any): Observable<any> {
        let url = this.config.getUaaEndpoint('api/account');
        return this.http.post(url, account);
    }
}
