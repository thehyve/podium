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
import { mergeMap } from 'rxjs/operators';

import { Account } from '../core/auth/account.model';
import { AccountService } from '../core/auth/account.service';
import { AuthServerProvider } from '../core/auth/auth-jwt.service';
import { Login } from './login.model';

@Injectable({ providedIn: 'root' })
export class LoginService {

    constructor(
        private accountService: AccountService,
        private authServerProvider: AuthServerProvider
    ) {}

    login(credentials: Login): Observable<Account | null> {
        return this.authServerProvider.login(credentials).pipe(mergeMap(() => this.accountService.identity(true)));
    }

    logout(): void {
        this.authServerProvider.logout().subscribe({ complete: () => this.accountService.authenticate(null) });
    }
}
