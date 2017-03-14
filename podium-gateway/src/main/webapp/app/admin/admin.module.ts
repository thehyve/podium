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
    adminRoute,
    AuditsComponent,
    LogsComponent,
    PdmMetricsMonitoringModalComponent,
    PdmMetricsMonitoringComponent,
    PdmHealthModalComponent,
    PdmHealthCheckComponent,
    PdmConfigurationComponent,
    PdmDocsComponent,
    AuditsService,
    PdmConfigurationService,
    PdmHealthService,
    PdmMetricsService,
    GatewayRoutesService,
    PdmGatewayComponent,
    LogsService,
} from './';

@NgModule({
    imports: [
        PodiumGatewaySharedModule,
        RouterModule.forRoot(adminRoute, { useHash: true })
    ],
    declarations: [
        AuditsComponent,
        LogsComponent,
        PdmConfigurationComponent,
        PdmHealthCheckComponent,
        PdmHealthModalComponent,
        PdmDocsComponent,
        PdmGatewayComponent,
        PdmMetricsMonitoringComponent,
        PdmMetricsMonitoringModalComponent
    ],
    entryComponents: [
        PdmHealthModalComponent,
        PdmMetricsMonitoringModalComponent,
    ],
    providers: [
        AuditsService,
        PdmConfigurationService,
        PdmHealthService,
        PdmMetricsService,
        GatewayRoutesService,
        LogsService,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayAdminModule {}
