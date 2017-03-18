/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { userMgmtRoute, userDialogRoute, UserResolvePagingParams, UserResolve } from './user-management.route';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { PodiumGatewaySharedModule } from '../../shared/shared.module';
import { PodiumGatewayAdminModule } from '../../admin/admin.module';
import { RouterModule } from '@angular/router';
import {
    UserMgmtComponent,
    UserDialogComponent,
    UserMgmtDialogComponent,
    UserDeleteDialogComponent,
    UserMgmtDeleteDialogComponent,
    UserUnlockDialogComponent,
    UserMgmtUnlockDialogComponent,
    UserMgmtDetailComponent,
    UserModalService
} from '../';

let BACKOFFICE_STATES = [
        ...userMgmtRoute,
        ...userDialogRoute
];

@NgModule({
    imports: [
        PodiumGatewaySharedModule,
        PodiumGatewayAdminModule,
        RouterModule.forRoot(BACKOFFICE_STATES, { useHash: true })
    ],
    declarations: [
        UserMgmtComponent,
        UserDialogComponent,
        UserDeleteDialogComponent,
        UserUnlockDialogComponent,
        UserMgmtDetailComponent,
        UserMgmtDialogComponent,
        UserMgmtDeleteDialogComponent,
        UserMgmtUnlockDialogComponent,
    ],
    entryComponents: [
        UserMgmtDialogComponent,
        UserMgmtDeleteDialogComponent,
        UserMgmtUnlockDialogComponent,
    ],
    providers: [
        UserResolvePagingParams,
        UserResolve,
        UserModalService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayUserMgmtModule {}
