/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { NgModule, Sanitizer } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { TranslateService } from '@ngx-translate/core';
import { AlertService } from 'ng-jhipster';
import {
    PodiumGatewaySharedLibsModule,
    JhiLanguageHelper,
    FindLanguageFromKeyPipe,
    JhiAlertComponent,
    JhiAlertErrorComponent
} from './';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';


export function alertServiceProvider(sanitizer: Sanitizer, translateService: TranslateService) {
    // set below to true to make alerts look like toast
    let isToast = false;
    return new AlertService(sanitizer, isToast, translateService);
}

@NgModule({
    imports: [
        PodiumGatewaySharedLibsModule,
        NgbModule
    ],
    declarations: [
        FindLanguageFromKeyPipe,
        JhiAlertComponent,
        JhiAlertErrorComponent
    ],
    providers: [
        JhiLanguageHelper,
        {
            provide: AlertService,
            useFactory: alertServiceProvider,
            deps: [Sanitizer, TranslateService]
        },
        Title
    ],
    exports: [
        PodiumGatewaySharedLibsModule,
        FindLanguageFromKeyPipe,
        JhiAlertComponent,
        JhiAlertErrorComponent
    ]
})
export class PodiumGatewaySharedCommonModule {}
