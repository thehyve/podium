/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { ElementRef, NgModule, Renderer2 } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { SessionStorageService } from 'ngx-webstorage';

import { AccountService } from '../../core/auth/account.service';
import { AlertService } from '../../core/util/alert.service';

import { makeAccountServiceMock } from './account-service.mock';
import { makeAlertServiceMock } from './alert-service.mock';
import { makeTranslateServiceMock } from './translate-service.mock';

@NgModule({
    imports: [
        HttpClientTestingModule
    ],
    providers: [
        FormBuilder,

        { provide: AccountService, useFactory: makeAccountServiceMock, },
        { provide: AlertService, useFactory: makeAlertServiceMock },
        {
            provide: ElementRef,
            useValue: null,
        },
        {
            provide: Renderer2,
            useValue: null,
        },
        {
            provide: Router,
            useValue: { navigate: () => { } },
        },
        {
            provide: SessionStorageService,
            useValue: {},
        },
        { provide: TranslateService, useFactory: makeTranslateServiceMock, }
    ],
})
export class PodiumTestModule { }
