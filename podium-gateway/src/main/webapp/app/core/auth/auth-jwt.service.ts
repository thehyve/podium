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
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { LocalStorageService, SessionStorageService } from 'ngx-webstorage';

import { ApplicationConfigService } from '../config/application-config.service';
import { Login } from '../../login/login.model';

type JwtToken = {
    id_token: string;
};

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
        return this.http
            .post<JwtToken>(this.applicationConfigService.getEndpointFor('api/authenticate'), credentials)
            .pipe(map(response => this.authenticateSuccess(response, credentials.rememberMe)));
    }

    logout(): Observable<void> {
        return new Observable(observer => {
            this.$localStorage.clear('authenticationToken');
            this.$sessionStorage.clear('authenticationToken');
            observer.complete();
        });
    }

    private authenticateSuccess(response: JwtToken, rememberMe: boolean): void {
        const jwt = response.id_token;
        if (rememberMe) {
            this.$localStorage.store('authenticationToken', jwt);
            this.$sessionStorage.clear('authenticationToken');
        } else {
            this.$sessionStorage.store('authenticationToken', jwt);
            this.$localStorage.clear('authenticationToken');
        }
    }
}
