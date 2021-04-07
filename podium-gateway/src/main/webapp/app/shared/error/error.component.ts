/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'pdm-error',
    templateUrl: './error.component.html'
})
export class PdmErrorComponent implements OnInit {
    errorMessage: string;
    error403: boolean;

    constructor(
    ) {

    }

    ngOnInit() {
    }
}
