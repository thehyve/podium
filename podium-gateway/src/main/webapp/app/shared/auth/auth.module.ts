/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { NgModule } from '@angular/core';
import { AccountService } from '../../core/auth/account.service';
import { SessionStorageService } from 'ngx-webstorage';
import { StateStorageService } from './state-storage.service';
import { AuthServerProvider } from './auth-jwt.service';
import { AuthService } from '../../core/auth/auth.service';
import { UserRouteAccessService } from '../../core/auth/user-route-access-service';

@NgModule({
    providers: [
        AccountService,
        AuthServerProvider,
        AuthService,
        SessionStorageService,
        StateStorageService,
        UserRouteAccessService
    ]
})
export class PodiumAuthModule {}
