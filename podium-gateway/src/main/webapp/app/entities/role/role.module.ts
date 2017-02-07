import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { PodiumGatewaySharedModule } from '../../shared';
import { PodiumGatewayAdminModule } from '../../admin/admin.module';
import { PodiumGatewayOrganisationModule } from '../organisation/organisation.module';

import {
    RoleService,
    RolePopupService,
    RoleComponent,
    RoleDetailComponent,
    RoleDialogComponent,
    RolePopupComponent,
    roleRoute,
    rolePopupRoute,
    RoleResolvePagingParams,
} from './';

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
