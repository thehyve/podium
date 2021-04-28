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
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';

import { LoginService } from '../../login/login.service';
import { StateStorageService } from '../auth/state-storage.service';
import { AccountService } from '../auth/account.service';

@Injectable()
export class AuthExpiredInterceptor implements HttpInterceptor {
    constructor(
        private loginService: LoginService,
        private stateStorageService: StateStorageService,
        private router: Router,
        private accountService: AccountService
    ) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(
            tap({
                error: (err: HttpErrorResponse) => {
                    if (err.status === 401 && err.url && !err.url.includes('zapi/account') && this.accountService.isAuthenticated()) {
                        this.stateStorageService.storeUrl(this.router.routerState.snapshot.url);
                        this.loginService.logout();
                        this.router.navigate(['/login']);
                    }
                },
            })
        );
    }
}
