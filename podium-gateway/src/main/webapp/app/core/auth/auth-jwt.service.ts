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
import { Observable } from 'rxjs/Rx';
import { LocalStorageService, SessionStorageService } from 'ngx-webstorage';

@Injectable({ providedIn: 'root' })
export class AuthServerProvider {
    constructor(
        private http: HttpClient,
        private $localStorage: LocalStorageService,
        private $sessionStorage: SessionStorageService
    ) {}

    getToken () {
        return this.$localStorage.retrieve('authenticationToken') || this.$sessionStorage.retrieve('authenticationToken');
    }

    login (credentials): Observable<any> {
        let data = new URLSearchParams();
        data.append('grant_type', 'password');
        data.append('username', credentials.username);
        data.append('password', credentials.password);

        let headers = {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization' : 'Basic d2ViX2FwcDo='
        };

        return this.http.post('podiumuaa/oauth/token', data, {
            headers
        }).map((resp) => {
            let accessToken = resp['access_token'];
            if (accessToken) {
                this.storeAuthenticationToken(accessToken, credentials.rememberMe);
            }

            return accessToken;
        });
    }

    loginWithToken(jwt, rememberMe) {
        if (jwt) {
            this.storeAuthenticationToken(jwt, rememberMe);
            return Promise.resolve(jwt);
        } else {
            return Promise.reject('auth-jwt-service Promise reject'); // Put appropriate error message here
        }
    }

    storeAuthenticationToken(jwt, rememberMe) {
        if (rememberMe) {
            this.$localStorage.store('authenticationToken', jwt);
        } else {
            this.$sessionStorage.store('authenticationToken', jwt);
        }
    }

    logout (): Observable<any> {
        return new Observable(observer => {
            this.$localStorage.clear('authenticationToken');
            this.$sessionStorage.clear('authenticationToken');
            observer.complete();
        });
    }
}
