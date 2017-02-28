/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Routes, CanActivate } from '@angular/router';
import { UserRouteAccessService } from '../shared';
import {
    organisationRoute,
    organisationPopupRoute,
    roleRoute,
    rolePopupRoute,
    userDialogRoute, userMgmtRoute } from './';

let ENTITY_ROUTES = [
    organisationRoute,
    organisationPopupRoute,
    roleRoute,
    rolePopupRoute,
    userMgmtRoute,
    userDialogRoute
];

export const entityState: Routes = [{
    path: '',
    data: {
        authorities: ['ROLE_PODIUM_ADMIN', 'ROLE_PODIUM_BBMRI']
    },
    canActivate: [UserRouteAccessService],
    children: ENTITY_ROUTES
}];
