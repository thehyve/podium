/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { NgModule, CUSTOM_ELEMENTS_SCHEMA, ModuleWithProviders } from '@angular/core';
import { DatePipe } from '@angular/common';
import { SessionStorageService } from 'ng2-webstorage';
import {
    PodiumGatewaySharedLibsModule,
    PodiumGatewaySharedCommonModule,
    UserService,
    LoginService,
    LoginModalService,
    MessageService,
    EmailValidatorDirective,
    PasswordValidatorDirective,
    PasswordMatchesDirective,
    WordLengthValidatorDirective,
    PodiumLoginComponent,
    SpecialismComponent
} from './';
import { RequestService } from './request/request.service';
import { EnumKeysPipe } from './pipes/enumKeys';
import { OrganisationSelectorComponent } from './organisation-selector/organisation-selector.component';
import { RequestAccessService } from './request/request-access.service';
import { RequestReviewPanelComponent } from './request/request-review-panel/request-review-panel.component';
import { PodiumEventMessageComponent } from './event/podium-event-message.component';
import { LinkedRequestNotificationComponent } from './linked-request-notification/linked-request-notification.component';
import { PodiumAuthModule } from './auth/auth.module';
import { FindLanguageFromKeyPipe } from './language/language.pipe';
import { ActiveMenuDirective } from './navbar/active-menu.directive';
import { CompletedComponent } from './completed/completed.component';
import { ErrorComponent } from './error/error.component';
import { NavbarComponent } from './navbar/navbar.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { ProfileService } from './profiles/profile.service';
import { RouterModule } from '@angular/router';

@NgModule({
    imports: [
        PodiumGatewaySharedLibsModule,
        PodiumGatewaySharedCommonModule,
        PodiumAuthModule,
        RouterModule
    ],
    declarations: [
        PodiumLoginComponent,
        SpecialismComponent,
        OrganisationSelectorComponent,
        RequestReviewPanelComponent,
        EmailValidatorDirective,
        PodiumEventMessageComponent,
        LinkedRequestNotificationComponent,
        PasswordValidatorDirective,
        PasswordMatchesDirective,
        WordLengthValidatorDirective,
        EnumKeysPipe,
        FindLanguageFromKeyPipe,
        ActiveMenuDirective,
        CompletedComponent,
        ErrorComponent,
        NavbarComponent,
        NotFoundComponent,
    ],
    providers: [
        ProfileService,
        LoginService,
        LoginModalService,
        MessageService,
        SessionStorageService,
        UserService,
        RequestService,
        RequestAccessService,
        DatePipe
    ],
    entryComponents: [PodiumLoginComponent],
    exports: [
        PodiumGatewaySharedCommonModule,
        PodiumGatewaySharedLibsModule,
        PodiumLoginComponent,
        SpecialismComponent,
        OrganisationSelectorComponent,
        RequestReviewPanelComponent,
        PodiumEventMessageComponent,
        LinkedRequestNotificationComponent,
        EmailValidatorDirective,
        PasswordValidatorDirective,
        PasswordMatchesDirective,
        WordLengthValidatorDirective,
        DatePipe,
        EnumKeysPipe,
        ActiveMenuDirective,
        FindLanguageFromKeyPipe,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],

})
export class PodiumGatewaySharedModule {

}
