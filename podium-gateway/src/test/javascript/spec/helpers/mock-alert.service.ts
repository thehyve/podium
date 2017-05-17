/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { SpyObject } from './spyobject';
import { AlertService } from 'ng-jhipster';
import Spy = jasmine.Spy;

export class MockAlertService extends SpyObject {

    successSpy: Spy;

    constructor() {
        super(AlertService);

        this.successSpy = this.spy('success').andReturn(true);
    }
}
