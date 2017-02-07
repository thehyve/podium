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
    Principal,
    HasAuthorityDirective,
    HasAnyAuthorityDirective,
    EmailValidatorDirective,
    PodiumLoginComponent
} from './';

@NgModule({
    imports: [
        PodiumGatewaySharedLibsModule,
        PodiumGatewaySharedCommonModule
    ],
    declarations: [
        PodiumLoginComponent,
        EmailValidatorDirective,
        HasAuthorityDirective,
        HasAnyAuthorityDirective
    ],
    providers: [
        CookieService,
        LoginService,
        LoginModalService,
        AccountService,
        StateStorageService,
        Principal,
        CSRFService,
        AuthServerProvider,
        AuthService,
        UserService,
        DatePipe
    ],
    entryComponents: [PodiumLoginComponent],
    exports: [
        PodiumGatewaySharedCommonModule,
        PodiumLoginComponent,
        EmailValidatorDirective,
        HasAuthorityDirective,
        HasAnyAuthorityDirective,
        DatePipe
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]

})
export class PodiumGatewaySharedModule {}
