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
import { organisationRoute } from './organisation.route';

let BACKOFFICE_ROUTES = [
    ...organisationRoute
];

@NgModule({
    imports: [
        RouterModule.forChild(BACKOFFICE_ROUTES)
    ],
    exports: [RouterModule]
})

export class OrganisationRoutingModule {}
