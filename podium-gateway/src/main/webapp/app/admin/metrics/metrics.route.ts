/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Route } from '@angular/router';
import { PdmMetricsMonitoringComponent } from './metrics.component';

export const metricsRoute: Route = {
    path: 'pdm-metrics',
    component: PdmMetricsMonitoringComponent,
    data: {
        pageTitle: 'metrics.title',
        breadcrumb: 'metrics'
    }
};
