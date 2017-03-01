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
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';

import {
    UploadPopupService,
    UploadPopupComponent,
    UploadDialogComponent,
    uploadPopupRoute
} from './';

import { FileSelectDirective, FileDropDirective } from 'ng2-file-upload';
import { FormsModule } from '@angular/forms';

let UPLOAD_STATES = [
    ...uploadPopupRoute
];

@NgModule({
    imports: [
        FormsModule,
        BrowserModule,
        CommonModule,
        RouterModule.forRoot(UPLOAD_STATES, { useHash: true })
    ],
    declarations: [
        UploadDialogComponent,
        UploadPopupComponent,
        FileDropDirective,
        FileSelectDirective
    ],
    entryComponents: [
        UploadDialogComponent,
        UploadPopupComponent
    ],
    providers: [
        UploadPopupService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumUploadModule {}
