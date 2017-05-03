/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { NgModule, CUSTOM_ELEMENTS_SCHEMA, ModuleWithProviders } from '@angular/core';
import { BreadcrumbComponent, BreadcrumbService } from './';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@NgModule({
    imports: [
        RouterModule,
        CommonModule
    ],
    declarations: [
        BreadcrumbComponent
    ],
    providers: [],
    exports: [
        BreadcrumbComponent
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],

})
export class BreadcrumbsModule {
    static forRoot(): ModuleWithProviders {
        return {
            ngModule: BreadcrumbsModule,
            providers: [
                BreadcrumbService
            ]
        };
    }
}
