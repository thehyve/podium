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
import { AccountService } from 'app/core/auth/account.service';
import Spy = jasmine.Spy;
import {of} from "rxjs";

export class MockAccountService extends SpyObject {

    getSpy: Spy;
    saveSpy: Spy;
    identitySpy: Spy;
    authenticationSpy: Spy;
    fakeResponse: any;

    constructor() {
        super(AccountService);

        this.fakeResponse = null;
        this.getSpy = this.spy('get').andReturn(this);
        this.saveSpy = this.spy('save').andReturn(this);
        this.identitySpy = this.spy('identity').andReturn(of(this.fakeResponse));
        this.authenticationSpy = this.spy('getAuthenticationState').andReturn(of(this.fakeResponse));
    }

    subscribe(callback: any) {
        callback(this.fakeResponse);
    }

    setResponse(json: any): void {
        this.fakeResponse = json;
    }
}
