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
    LogsService
} from './';
import { PdmElasticsearchComponent } from './elasticsearch/elasticsearch.component';
import { PdmElasticsearchModalComponent } from './elasticsearch/elasticsearch-modal.component';
import { PdmElasticsearchService } from './elasticsearch/elasticsearch.service';

@NgModule({
    imports: [
        PodiumGatewaySharedModule,
        RouterModule.forChild(adminRoute)
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
        PdmMetricsMonitoringModalComponent,
        PdmElasticsearchComponent,
        PdmElasticsearchModalComponent
    ],
    entryComponents: [
        PdmHealthModalComponent,
        PdmElasticsearchModalComponent,
        PdmMetricsMonitoringModalComponent,
    ],
    providers: [
        AuditsService,
        PdmConfigurationService,
        PdmHealthService,
        PdmMetricsService,
        GatewayRoutesService,
        LogsService,
        PdmElasticsearchService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayAdminModule {}
