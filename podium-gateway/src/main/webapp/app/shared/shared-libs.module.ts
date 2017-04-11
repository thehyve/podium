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
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { CommonModule } from '@angular/common';
import { NgJhipsterModule } from 'ng-jhipster';
import { InfiniteScrollModule } from 'angular2-infinite-scroll';
import { PodiumUploadModule } from './upload/upload.module';
import { TooltipModule } from 'ng2-bootstrap/tooltip';
import { TabsModule } from 'ng2-bootstrap/tabs';
import { UiSwitchModule } from 'angular2-ui-switch';

@NgModule({
    imports: [
        NgJhipsterModule.forRoot({
            i18nEnabled: true,
            defaultI18nLang: 'en'
        }),
        InfiniteScrollModule,
        TooltipModule.forRoot(),
        TabsModule.forRoot(),
        UiSwitchModule
    ],
    exports: [
        FormsModule,
        HttpModule,
        CommonModule,
        NgJhipsterModule,
        PodiumUploadModule,
        InfiniteScrollModule,
        TooltipModule,
        TabsModule,
        UiSwitchModule
    ]
})
export class PodiumGatewaySharedLibsModule {}
