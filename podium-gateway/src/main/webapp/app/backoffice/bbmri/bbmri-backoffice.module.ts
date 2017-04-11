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

import { BbmriBackofficeRoutingModule } from './bbmri-backoffice.routing';

@NgModule({
    imports: [
        BbmriBackofficeRoutingModule
    ],
    providers: [],
    exports: [
        RouterModule
    ]
})
export class PodiumGatewayBbmriBackofficeModule {}
