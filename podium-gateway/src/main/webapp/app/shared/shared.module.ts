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
import { TranslateModule } from '@ngx-translate/core';
import { SessionStorageService } from 'ngx-webstorage';
import { PodiumGatewaySharedCommonModule } from './shared-common.module';
import { PodiumGatewaySharedLibsModule } from './shared-libs.module';
import { EmailValidatorDirective } from './validators/email-validator.directive';
import { PasswordMatchesDirective } from './validators/password-matches.directive';
import { PasswordValidatorDirective } from './validators/password-validator.directive';
import { WordLengthValidatorDirective } from './validators/word-length-validator.directive';
import { SpecialismComponent } from './specialism/specialism.component';
import { EnumKeysPipe } from './pipes/enumKeys';
import { OrganisationSelectorComponent } from './organisation-selector/organisation-selector.component';
import { RequestReviewPanelComponent } from './request/request-review-panel/request-review-panel.component';
import { PodiumEventMessageComponent } from './event/podium-event-message.component';
import { LinkedRequestNotificationComponent } from './linked-request-notification/linked-request-notification.component';
import { CompletedComponent } from './completed/completed.component';
import { PdmErrorComponent } from '../layouts/error/error.component';
import { NavbarComponent } from '../layouts/navbar/navbar.component';
import { RouterModule } from '@angular/router';
import { AttachmentComponent } from './attachment/upload-attachment/attachment.component';
import { NgxUploaderModule } from 'ngx-uploader';
import { AttachmentListComponent } from './attachment/attachment-list/attachment-list.component';
import { PodiumLoginComponent } from '../login/login.component';
import { HasAnyAuthorityDirective } from './auth/has-any-authority.directive';
import { SortByDirective } from './sort/sort-by.directive';
import { SortDirective } from './sort/sort.directive';
import { ItemCountComponent } from './pagination/item-count.component';

@NgModule({
    imports: [
        PodiumGatewaySharedLibsModule,
        PodiumGatewaySharedCommonModule,
        RouterModule,
        TranslateModule.forChild(),
        NgxUploaderModule,
    ],
    declarations: [
        AttachmentComponent,
        PodiumLoginComponent,
        SpecialismComponent,
        OrganisationSelectorComponent,
        RequestReviewPanelComponent,
        EmailValidatorDirective,
        PodiumEventMessageComponent,
        LinkedRequestNotificationComponent,
        HasAnyAuthorityDirective,
        PasswordValidatorDirective,
        PasswordMatchesDirective,
        WordLengthValidatorDirective,
        EnumKeysPipe,
        CompletedComponent,
        PdmErrorComponent,
        NavbarComponent,
        AttachmentListComponent,
        SortByDirective,
        SortDirective,
        ItemCountComponent,
    ],
    providers: [
        SessionStorageService,
        DatePipe
    ],
    exports: [
        PodiumGatewaySharedCommonModule,
        PodiumGatewaySharedLibsModule,
        PodiumLoginComponent,
        SpecialismComponent,
        OrganisationSelectorComponent,
        AttachmentComponent,
        AttachmentListComponent,
        RequestReviewPanelComponent,
        PodiumEventMessageComponent,
        LinkedRequestNotificationComponent,
        EmailValidatorDirective,
        HasAnyAuthorityDirective,
        PasswordValidatorDirective,
        PasswordMatchesDirective,
        WordLengthValidatorDirective,
        SortByDirective,
        SortDirective,
        DatePipe,
        EnumKeysPipe,
        ItemCountComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],

})
export class PodiumGatewaySharedModule {

}
