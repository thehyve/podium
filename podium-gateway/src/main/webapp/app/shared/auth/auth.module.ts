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
import { AccountService } from './account.service';
import { SessionStorageService } from 'ng2-webstorage';
import { StateStorageService } from './state-storage.service';
import { Principal } from './principal.service';
import { CSRFService } from './csrf.service';
import { AuthServerProvider } from './auth-jwt.service';
import { AuthService } from './auth.service';
import { HasAnyAuthorityDirective } from './has-any-authority.directive';
import { UserRouteAccessService } from './user-route-access-service';

@NgModule({
    declarations: [
        HasAnyAuthorityDirective,
    ],
    exports: [
        HasAnyAuthorityDirective,
    ],
    providers: [
        AccountService,
        AuthServerProvider,
        AuthService,
        CSRFService,
        Principal,
        SessionStorageService,
        StateStorageService,
        UserRouteAccessService

    ]
})
export class PodiumAuthModule {}
