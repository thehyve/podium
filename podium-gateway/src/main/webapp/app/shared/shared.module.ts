/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { DatePipe } from '@angular/common';

import { CookieService } from 'angular2-cookie/services/cookies.service';
import {
    PodiumGatewaySharedLibsModule,
    PodiumGatewaySharedCommonModule,
    CSRFService,
    AuthService,
    AuthServerProvider,
    AccountService,
    UserService,
    StateStorageService,
    LoginService,
    LoginModalService,
    MessageService,
    Principal,
    HasAuthorityDirective,
    HasAnyAuthorityDirective,
    EmailValidatorDirective,
    PasswordValidatorDirective,
    PasswordMatchesDirective,
    WordLengthValidatorDirective,
    PodiumLoginComponent,
    SpecialismComponent
} from './';
import { AttachmentService } from './attachment/attachment.service';
import { RequestService } from './request/request.service';

@NgModule({
    imports: [
        PodiumGatewaySharedLibsModule,
        PodiumGatewaySharedCommonModule
    ],
    declarations: [
        PodiumLoginComponent,
        SpecialismComponent,
        EmailValidatorDirective,
        PasswordValidatorDirective,
        PasswordMatchesDirective,
        WordLengthValidatorDirective,
        HasAuthorityDirective,
        HasAnyAuthorityDirective
    ],
    providers: [
        CookieService,
        LoginService,
        LoginModalService,
        MessageService,
        AccountService,
        StateStorageService,
        Principal,
        CSRFService,
        AuthServerProvider,
        AuthService,
        UserService,
        AttachmentService,
        RequestService,
        DatePipe
    ],
    entryComponents: [PodiumLoginComponent],
    exports: [
        PodiumGatewaySharedCommonModule,
        PodiumLoginComponent,
        SpecialismComponent,
        EmailValidatorDirective,
        PasswordValidatorDirective,
        PasswordMatchesDirective,
        WordLengthValidatorDirective,
        HasAuthorityDirective,
        HasAnyAuthorityDirective,
        DatePipe
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]

})
export class PodiumGatewaySharedModule {}
