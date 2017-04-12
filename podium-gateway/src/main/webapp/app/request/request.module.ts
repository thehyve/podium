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
import { RouterModule } from '@angular/router';

import { PodiumGatewaySharedModule } from '../shared';

import {
    requestRoute,
    RequestFormService,
    RequestFormComponent,
    RequestFormSubmitDialogComponent,
} from './';
import { RequestOverviewComponent } from './overview/request-overview.component';
import { RequestOverviewService } from './overview/request-overview.service';
import { RequestMainDetailComponent } from './main-detail/request-main-detail.component';
import { RequestDetailComponent } from './main-detail/detail/request-detail.component';
import { CommonModule } from '@angular/common';

@NgModule({
    imports: [
        CommonModule,
        PodiumGatewaySharedModule,
        RouterModule.forChild(requestRoute)
    ],
    declarations: [
        RequestFormComponent,
        RequestFormSubmitDialogComponent,
        RequestOverviewComponent,
        RequestMainDetailComponent,
        RequestDetailComponent
    ],
    entryComponents: [
        RequestFormSubmitDialogComponent
    ],
    providers: [
        RequestFormService,
        RequestOverviewService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayRequestModule {}
