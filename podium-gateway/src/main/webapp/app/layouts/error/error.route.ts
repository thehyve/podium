/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Routes } from '@angular/router';
import { PdmErrorComponent } from './error.component';

export const errorRoute: Routes = [
    {
        path: 'error',
        component: PdmErrorComponent,
        data: {
            pageTitle: 'error.title'
        },
    },
    {
        path: 'accessdenied',
        component: PdmErrorComponent,
        data: {
            pageTitle: 'error.title',
            errorMessage: 'You are not authorized to access this page.',
        },
    },
    {
        path: '404',
        component: PdmErrorComponent,
        data: {
            pageTitle: 'Error page!',
            errorMessage: 'The page does not exist.',
        },
    },
    {
        path: '**',
        redirectTo: '/404',
    },
];
