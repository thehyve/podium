import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { DatePipe } from '@angular/common';

import { CookieService } from 'angular2-cookie/services/cookies.service';
import {
    PodiumSharedLibsModule,
    PodiumSharedCommonModule,
    CSRFService,
    AuthService,
    AuthServerProvider,
    AccountService,
    StateStorageService,
    LoginService,
    LoginModalService,
    Principal,
    HasAuthorityDirective,
    HasAnyAuthorityDirective,
    PodiumLoginComponent
} from './';

@NgModule({
    imports: [
        PodiumSharedLibsModule,
        PodiumSharedCommonModule
    ],
    declarations: [
        PodiumLoginComponent,
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
        DatePipe
    ],
    entryComponents: [PodiumLoginComponent],
    exports: [
        PodiumSharedCommonModule,
        PodiumLoginComponent,
        HasAuthorityDirective,
        HasAnyAuthorityDirective,
        DatePipe
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]

})
export class PodiumSharedModule {}
