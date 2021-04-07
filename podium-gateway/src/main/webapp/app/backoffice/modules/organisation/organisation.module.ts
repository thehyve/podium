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
import { PodiumGatewaySharedModule } from '../../../shared';
import {
    OrganisationPopupService,
    OrganisationComponent,
    OrganisationDeleteDialogComponent,
    OrganisationDeletePopupComponent,
    OrganisationResolvePagingParams
} from './';
import { OrganisationRoutingModule } from './organisation.routing';
import { customHttpProvider } from '../../../blocks/interceptor/http.provider';
import { RouterModule } from '@angular/router';
import { PodiumGatewayAdminModule } from '../../../admin/admin.module';
import { OrganisationFormComponent } from './organisation-form/organisation-form.component';
import { RoleAssignComponent } from '../../../shared/role/role-assign/role-assign.component';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { Http } from '@angular/http';
import { HttpLoaderFactory } from '../../../shared/shared-libs.module';
import { OrganisationService } from '../../../shared/organisation/organisation.service';

@NgModule({
    imports: [
        TranslateModule.forChild({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [Http]
            }
        }),
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
        customHttpProvider(),
        OrganisationService,
        OrganisationPopupService,
        OrganisationResolvePagingParams,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        RouterModule,
        TranslateModule
    ]
})
export class PodiumGatewayOrganisationModule {}
