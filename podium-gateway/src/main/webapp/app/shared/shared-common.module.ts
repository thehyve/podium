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
import {
    PodiumGatewaySharedLibsModule,
    PdmAlertComponent,
    PdmAlertErrorComponent
} from './';

@NgModule({
    imports: [
        PodiumGatewaySharedLibsModule,
    ],
    declarations: [
        PdmAlertComponent,
        PdmAlertErrorComponent
    ],
    exports: [
        PodiumGatewaySharedLibsModule,
        PdmAlertComponent,
        PdmAlertErrorComponent
    ]
})
export class PodiumGatewaySharedCommonModule {}
