/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PodiumGatewayOrganisationModule } from '../modules/organisation/organisation.module';
import { PodiumGatewayUserMgmtModule } from '../modules/user-management/user-management.module';

let BBMRI_ROUTES = [
    {
        path: 'bbmri',
        children: [
            {
                path: 'organisation',
                loadChildren: () => PodiumGatewayOrganisationModule
            },
            {
                path: 'user-management',
                loadChildren: () => PodiumGatewayUserMgmtModule
            }
        ]
    }
];

@NgModule({
    imports: [
        RouterModule.forChild(BBMRI_ROUTES)
    ],
    exports: [RouterModule]
})

export class BbmriBackofficeRoutingModule {}
