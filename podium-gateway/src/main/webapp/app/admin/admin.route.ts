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

import {
    auditsRoute,
    configurationRoute,
    docsRoute,
    healthRoute,
    logsRoute,
    metricsRoute,
    gatewayRoute
} from './';

import { UserRouteAccessService } from '../shared';
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
