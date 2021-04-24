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
import { BehaviorSubject, Observable } from 'rxjs';
import { Account } from './account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
    private userIdentity: Account | null = null;
    private authenticationState = new BehaviorSubject<any>(null);

    constructor(private http: HttpClient) { }

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

        return this.identity().then(id => {
            return Promise.resolve(id.authorities && id.authorities.indexOf(authority) !== -1);
        }, () => {
            return Promise.resolve(false);
        });
    }

    identity(force?: boolean): Promise<Account | null> {
        if (force === true) {
            this.userIdentity = undefined;
        }

        // check and see if we have retrieved the _identity data from the server.
        // if we have, reuse it by immediately resolving
        if (this.userIdentity) {
            return Promise.resolve(this.userIdentity);
        }

        // retrieve the _identity data from the server, update the _identity object, and then resolve.
        return this.get().toPromise().then(account => {
            this.authenticate(account);
            return this.userIdentity;
        }).catch(err => {
            this.authenticate(null);;
            return null;
        });
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
        return this.http.get<Account>('podiumuaa/api/account');
    }

    save(account: any): Observable<any> {
        return this.http.post('podiumuaa/api/account', account);
    }
}
