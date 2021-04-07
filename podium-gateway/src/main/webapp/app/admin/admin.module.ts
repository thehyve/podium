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
import { PodiumGatewaySharedModule } from '../shared/shared.module';
import { adminRoute } from './admin-routing.module';
import {
    AuditsComponent,
    LogsComponent,
    PdmMetricsMonitoringModalComponent,
    PdmMetricsMonitoringComponent,
    PdmHealthModalComponent,
    PdmHealthCheckComponent,
    PdmConfigurationComponent,
    PdmDocsComponent,
    PdmGatewayComponent,
} from './';
import { PdmElasticsearchComponent } from './elasticsearch/elasticsearch.component';
import { PdmElasticsearchModalComponent } from './elasticsearch/elasticsearch-modal.component';

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
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayAdminModule {}
