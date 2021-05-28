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
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { LocalStorageService, SessionStorageService } from 'ngx-webstorage';

import { ApplicationConfigService } from '../config/application-config.service';
import { Login } from '../../login/login.model';

@Injectable({ providedIn: 'root' })
export class AuthServerProvider {
    constructor(
        private http: HttpClient,
        private $localStorage: LocalStorageService,
        private $sessionStorage: SessionStorageService,
        private applicationConfigService: ApplicationConfigService
    ) { }

    getToken(): string {
        const tokenInLocalStorage: string | null = this.$localStorage.retrieve('authenticationToken');
        const tokenInSessionStorage: string | null = this.$sessionStorage.retrieve('authenticationToken');
        return tokenInLocalStorage ?? tokenInSessionStorage ?? '';
    }

    login(credentials: Login): Observable<void> {
        const data =
            `grant_type=password` +
            `&username=${encodeURIComponent(credentials.username)}` +
            `&password=${encodeURIComponent(credentials.password)}`;

        const headers = new HttpHeaders()
            .set('Content-Type', 'application/x-www-form-urlencoded')
            .set('Authorization', 'Basic d2ViX2FwcDo=');

        let url = this.applicationConfigService.getUaaEndpoint('oauth/token');
        return this.http.post<void>(url, data, { headers }).pipe(map((resp) => {
            let accessToken = resp['access_token'];
            if (accessToken) {
                this.storeAuthenticationToken(accessToken, credentials.rememberMe);
            }

            return accessToken;
        }));
    }

    logout(): Observable<void> {
        return new Observable(observer => {
            this.$localStorage.clear('authenticationToken');
            this.$sessionStorage.clear('authenticationToken');
            observer.complete();
        });
    }

    storeAuthenticationToken(jwt, rememberMe) {
        if (rememberMe) {
            this.$localStorage.store('authenticationToken', jwt);
        } else {
            this.$sessionStorage.store('authenticationToken', jwt);
        }
    }
}
