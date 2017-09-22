/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { RouterModule } from '@angular/router';
import { NotFoundComponent } from './not-found.component';
import { NgModule } from '@angular/core';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '**',
                component: NotFoundComponent
            }
        ])
    ],
    declarations: [
        NotFoundComponent
    ],
    exports: [
        RouterModule
    ]
})
export class NotFoundRoutingModule { }
