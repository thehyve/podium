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
import { organisationRoute, organisationPopupRoute, OrganisationResolvePagingParams } from './organisation.route';
import { OrganisationComponent } from './organisation.component';
import { OrganisationDetailComponent } from './organisation-detail.component';
import { OrganisationDialogComponent, OrganisationPopupComponent } from './organisation-dialog.component';
import {
    OrganisationDeleteDialogComponent,
    OrganisationDeletePopupComponent
} from './organisation-delete-dialog.component';
import { OrganisationService } from './organisation.service';
import { OrganisationPopupService } from './organisation-popup.service';

let ENTITY_STATES = [
    ...organisationRoute,
    ...organisationPopupRoute,
];

@NgModule({
    imports: [
        PodiumGatewaySharedModule,
        RouterModule.forRoot(ENTITY_STATES, { useHash: true })
    ],
    declarations: [
        OrganisationComponent,
        OrganisationDetailComponent,
        OrganisationDialogComponent,
        OrganisationDeleteDialogComponent,
        OrganisationPopupComponent,
        OrganisationDeletePopupComponent,
    ],
    entryComponents: [
        OrganisationComponent,
        OrganisationDialogComponent,
        OrganisationPopupComponent,
        OrganisationDeleteDialogComponent,
        OrganisationDeletePopupComponent,
    ],
    providers: [
        OrganisationService,
        OrganisationPopupService,
        OrganisationResolvePagingParams,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayOrganisationModule {}
