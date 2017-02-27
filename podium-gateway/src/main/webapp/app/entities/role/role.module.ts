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

import { PodiumGatewaySharedModule } from '../../shared';
import { PodiumGatewayAdminModule } from '../../admin/admin.module';
import { PodiumGatewayOrganisationModule } from '../organisation/organisation.module';
import { roleRoute, rolePopupRoute, RoleResolvePagingParams } from './role.route';
import { RoleComponent } from './role.component';
import { RoleDetailComponent } from './role-detail.component';
import { RoleDialogComponent, RolePopupComponent } from './role-dialog.component';
import { RoleService } from './role.service';
import { RolePopupService } from './role-popup.service';



let ENTITY_STATES = [
    ...roleRoute,
    ...rolePopupRoute,
];

@NgModule({
    imports: [
        PodiumGatewaySharedModule,
        PodiumGatewayAdminModule,
        PodiumGatewayOrganisationModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true })
    ],
    declarations: [
        RoleComponent,
        RoleDetailComponent,
        RoleDialogComponent,
        RolePopupComponent,
    ],
    entryComponents: [
        RoleComponent,
        RoleDialogComponent,
        RolePopupComponent,
    ],
    providers: [
        RoleService,
        RolePopupService,
        RoleResolvePagingParams,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayRoleModule {}
