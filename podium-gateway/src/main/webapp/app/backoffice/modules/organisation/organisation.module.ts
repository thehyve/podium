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
import { TranslateModule } from '@ngx-translate/core';
import { PodiumGatewaySharedModule } from '../../../shared/shared.module';
import { OrganisationComponent } from './organisation.component';
import {
    OrganisationDeleteDialogComponent,
    OrganisationDeletePopupComponent
} from './organisation-delete-dialog.component';
import { OrganisationResolvePagingParams } from './organisation.route';
import { OrganisationRoutingModule } from './organisation.routing';
import { RouterModule } from '@angular/router';
import { PodiumGatewayAdminModule } from '../../../admin/admin.module';
import { OrganisationFormComponent } from './organisation-form/organisation-form.component';
import { RoleAssignComponent } from '../../../shared/role/role-assign/role-assign.component';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';

@NgModule({
    imports: [
        TranslateModule.forChild(),
        PodiumGatewaySharedModule,
        PodiumGatewayAdminModule,
        OrganisationRoutingModule,
        TypeaheadModule,
    ],
    declarations: [
        OrganisationComponent,
        OrganisationFormComponent,
        OrganisationDeleteDialogComponent,
        OrganisationDeletePopupComponent,
        RoleAssignComponent
    ],
    entryComponents: [
        OrganisationComponent,
        OrganisationFormComponent,
        OrganisationDeleteDialogComponent,
        OrganisationDeletePopupComponent,
    ],
    providers: [
        OrganisationResolvePagingParams,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        RouterModule,
        TranslateModule
    ]
})
export class PodiumGatewayOrganisationModule {}
