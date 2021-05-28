/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Routes } from '@angular/router';
import { auditsRoute } from './audits/audits.route';
import { configurationRoute } from './configuration/configuration.route';
import { docsRoute } from './docs/docs.route';
import { healthRoute } from './health/health.route';
import { logsRoute } from './logs/logs.route';
import { metricsRoute } from './metrics/metrics.route';
import { gatewayRoute } from './gateway/gateway.route';
import { UserRouteAccessService } from '../core/auth/user-route-access.service';
import { elasticsearchRoute } from './elasticsearch/elasticsearch.route';

let ADMIN_ROUTES = [
    auditsRoute,
    configurationRoute,
    docsRoute,
    healthRoute,
    logsRoute,
    gatewayRoute,
    metricsRoute,
    elasticsearchRoute
];

export const adminRoute: Routes = [{
    path: 'admin',
    data: {
        authorities: ['ROLE_PODIUM_ADMIN'],
        breadcrumb: 'admin'
    },
    canActivate: [UserRouteAccessService],
    children: ADMIN_ROUTES
}];
