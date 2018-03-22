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
import { JhiAlertService } from 'ng-jhipster';
import {
    PodiumGatewaySharedLibsModule,
    JhiLanguageHelper,
    PdmAlertComponent,
    PdmAlertErrorComponent
} from './';
import { JhiConfigService } from 'ng-jhipster/src/config.service';

export function alertServiceProvider(sanitizer: Sanitizer, translateService: TranslateService) {
    // set below to true to make alerts look like toast
    let jhiConfigService = new JhiConfigService();
    jhiConfigService.CONFIG_OPTIONS.alertAsToast = false;
    return new JhiAlertService(sanitizer, jhiConfigService, translateService);
}

@NgModule({
    imports: [
        PodiumGatewaySharedLibsModule,
    ],
    declarations: [
        PdmAlertComponent,
        PdmAlertErrorComponent
    ],
    providers: [
        JhiLanguageHelper,
        {
            provide: JhiAlertService,
            useFactory: alertServiceProvider,
            deps: [Sanitizer, TranslateService]
        },
        Title
    ],
    exports: [
        PodiumGatewaySharedLibsModule,
        PdmAlertComponent,
        PdmAlertErrorComponent
    ]
})
export class PodiumGatewaySharedCommonModule {}
