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
    RequestFormComponent
} from './';

import { EnumKeysPipe } from '../shared/pipes/enumKeys';

@NgModule({
    imports: [
        PodiumGatewaySharedModule,
        RouterModule.forChild(requestRoute)
    ],
    declarations: [
        RequestFormComponent,
        EnumKeysPipe
    ],
    providers: [
        RequestFormService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayRequestModule {}
