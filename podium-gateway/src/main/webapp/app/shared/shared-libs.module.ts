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
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        TooltipModule.forRoot(),
        TabsModule.forRoot(),
    ],
    exports: [
        CommonModule,
        NgbModule,
        FormsModule,
        CommonModule,
        InfiniteScrollModule,
        TooltipModule,
        TabsModule,
        FontAwesomeModule,
        ReactiveFormsModule,
    ],
})
export class PodiumGatewaySharedLibsModule { }

