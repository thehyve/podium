/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { UserResolvePagingParams, UserResolve } from './user-management.route';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { PodiumGatewaySharedModule } from '../../../shared/shared.module';
import { PodiumGatewayAdminModule } from '../../../admin/admin.module';
import { RouterModule } from '@angular/router';
import {
    UserMgmtComponent,
    UserDialogComponent,
    UserMgmtDialogComponent,
    UserDeleteDialogComponent,
    UserMgmtDeleteDialogComponent,
    UserUnlockDialogComponent,
    UserMgmtUnlockDialogComponent,
} from './';
import { customHttpProvider } from '../../../blocks/interceptor/http.provider';
import { UserMgmtRoutingModule } from './user-management.routing';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { Http } from '@angular/http';
import { HttpLoaderFactory } from '../../../shared/shared-libs.module';

@NgModule({
    imports: [
        TranslateModule.forChild({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [Http]
            }
        }),
        PodiumGatewayAdminModule,
        PodiumGatewaySharedModule,
        UserMgmtRoutingModule,
    ],
    declarations: [
        UserMgmtComponent,
        UserDialogComponent,
        UserDeleteDialogComponent,
        UserUnlockDialogComponent,
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
        customHttpProvider(),
        UserResolvePagingParams,
        UserResolve,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        RouterModule,
        TranslateModule
    ]
})
export class PodiumGatewayUserMgmtModule {}
