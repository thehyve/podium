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
import { Observable } from 'rxjs';
import { LocalStorageService, SessionStorageService } from 'ngx-webstorage';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';

import { ApplicationConfigService } from '../config/application-config.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    constructor(
        private localStorage: LocalStorageService,
        private sessionStorage: SessionStorageService,
        private applicationConfigService: ApplicationConfigService
    ) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let doNotInjectBearerTokenForUrls = [
            // Login
            'oauth/token',
            // Password reset
            'api/account/reset_password/finish',
            'api/account/reset_password/init',
            // Registration and email confirmation
            'api/register',
            'api/verify',
            'api/reverify',
        ].map(e => this.applicationConfigService.getUaaEndpoint(e));
        if (doNotInjectBearerTokenForUrls.includes(request.url)) {
            return next.handle(request);
        }
        if (request.url.startsWith('i18n/')) {
            return next.handle(request);
        }
        
        const token: string | null = this.localStorage.retrieve('authenticationToken') ?? this.sessionStorage.retrieve('authenticationToken');
        if (token) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`,
                },
            });
        }
        return next.handle(request);
    }
}
