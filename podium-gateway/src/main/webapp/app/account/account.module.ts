/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { HttpClient } from '@angular/common/http';
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { translatePartialLoader } from '../config/translation.config';
import { PodiumGatewaySharedModule } from '../shared/shared.module';
import { accountState } from './account.route';
import { PasswordResetFinishComponent } from './password-reset/finish/password-reset-finish.component';
import { PasswordResetInitComponent } from './password-reset/init/password-reset-init.component';
import { PasswordStrengthBarComponent } from './password/password-strength-bar.component';
import { PasswordComponent } from './password/password.component';
import { RegisterComponent } from './register/register.component';
import { SettingsComponent } from './settings/settings.component';
import { VerifyComponent } from './verify/verify.component';

@NgModule({
    imports: [
        PodiumGatewaySharedModule,
        TranslateModule.forChild({
            loader: {
                provide: TranslateLoader,
                useFactory: translatePartialLoader,
                deps: [HttpClient],
            }
        }),
        RouterModule.forChild(accountState)
    ],
    declarations: [
        VerifyComponent,
        RegisterComponent,
        PasswordComponent,
        PasswordStrengthBarComponent,
        PasswordResetInitComponent,
        PasswordResetFinishComponent,
        SettingsComponent
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayAccountModule {}
