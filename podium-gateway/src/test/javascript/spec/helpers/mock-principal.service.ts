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
import { Principal } from '../../../../main/webapp/app/shared/auth/principal.service';
import Spy = jasmine.Spy;

export class MockPrincipal extends SpyObject {

    identitySpy: Spy;
    fakeResponse: any;

    constructor() {
        super(Principal);

        this.fakeResponse = {};
        this.identitySpy = this.spy('identity').andReturn(Promise.resolve(this.fakeResponse));
    }

    setResponse(json: any): void {
        this.fakeResponse = json;
    }
}
