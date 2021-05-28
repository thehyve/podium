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
import { TranslateModule } from '@ngx-translate/core';
import { PodiumGatewaySharedModule } from '../shared/shared.module';
import { adminRoute } from './admin-routing.module';
import { AuditsComponent } from './audits/audits.component';
import { LogsComponent } from './logs/logs.component';
import { PdmHealthCheckComponent } from './health/health.component';
import { PdmConfigurationComponent } from './configuration/configuration.component';
import { PdmDocsComponent } from './docs/docs.component';
import { PdmGatewayComponent } from './gateway/gateway.component';
import { PdmElasticsearchComponent } from './elasticsearch/elasticsearch.component';
import { PdmElasticsearchModalComponent } from './elasticsearch/elasticsearch-modal.component';

@NgModule({
    imports: [
        PodiumGatewaySharedModule,
        TranslateModule.forChild(),
        RouterModule.forChild(adminRoute)
    ],
    declarations: [
        AuditsComponent,
        LogsComponent,
        PdmConfigurationComponent,
        PdmHealthCheckComponent,
        PdmDocsComponent,
        PdmGatewayComponent,
        PdmElasticsearchComponent,
        PdmElasticsearchModalComponent
    ],
    entryComponents: [
        PdmElasticsearchModalComponent
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class PodiumGatewayAdminModule { }
