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
import { Subscriber } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';
import Spy = jasmine.Spy;

export class MockEventManager extends SpyObject {

    subscriber: Subscriber<any>;

    constructor() {
        super(JhiEventManager);

        this.subscriber = new Subscriber();
    }
}
