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
import { BehaviorSubject, Observable } from 'rxjs/Rx';
import { Account } from '../../core/auth/account.model';

@Injectable()
export class AccountService  {
    private _identity: Account;
    private authenticationState = new BehaviorSubject<any>(null);

    constructor(private http: HttpClient) { }

    authenticate(_identity: Account | null) {
        this._identity = _identity;
        this.authenticationState.next(this._identity);
    }

    hasAnyAuthority(authorities: string[]): boolean {
        if (!this._identity || !this._identity.authorities) {
            return false;
        }
        return this._identity.authorities.some((authority: string) => authorities.includes(authority));
    }

    hasAuthority (authority: string): Promise<boolean> {
        if (!this._identity) {
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
            this._identity = undefined;
        }

        // check and see if we have retrieved the _identity data from the server.
        // if we have, reuse it by immediately resolving
        if (this._identity) {
            return Promise.resolve(this._identity);
        }

        // retrieve the _identity data from the server, update the _identity object, and then resolve.
        return this.get().toPromise().then(account => {
            this.authenticate(account);
            return this._identity;
        }).catch(err => {
            this.authenticate(null);;
            return null;
        });
    }

    isAuthenticated (): boolean {
        return this._identity !== null;
    }

    getAuthenticationState(): BehaviorSubject<any> {
        return this.authenticationState;
    }

    isIdentityResolved (): boolean {
        return this._identity !== undefined;
    }

    getImageUrl(): String {
        return this.isIdentityResolved () ? this._identity.imageUrl : null;
    }

    private get(): Observable<Account> {
        return this.http.get<Account>('podiumuaa/api/account');
    }

    save(account: any): Observable<any> {
        return this.http.post('podiumuaa/api/account', account);
    }
}
