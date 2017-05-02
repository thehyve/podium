/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

export class MockActivatedRoute extends ActivatedRoute {

    constructor(parameters?: any) {
        super();
        this.queryParams = Observable.of(parameters);
        this.params = Observable.of(parameters);
    }
}

export class MockRouter {
    navigate = jasmine.createSpy('navigate');
}
