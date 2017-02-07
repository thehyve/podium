import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { PodiumGatewaySharedModule } from '../../shared';

import {
    OrganisationService,
    OrganisationPopupService,
    OrganisationComponent,
    OrganisationDetailComponent,
    OrganisationDialogComponent,
    OrganisationPopupComponent,
    OrganisationDeletePopupComponent,
    OrganisationDeleteDialogComponent,
    organisationRoute,
    organisationPopupRoute,
    OrganisationResolvePagingParams,
} from './';

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
