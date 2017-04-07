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

import { EnumKeysPipe } from '../shared/pipes/enumKeys';
import { RequestOverviewComponent } from './overview/request-overview.component';
import { RequestOverviewService } from './overview/request-overview.service';

@NgModule({
    imports: [
        PodiumGatewaySharedModule,
        RouterModule.forRoot(requestRoute, { useHash: true })
    ],
    declarations: [
        RequestFormComponent,
        RequestFormSubmitDialogComponent,
        RequestOverviewComponent,
        EnumKeysPipe
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
