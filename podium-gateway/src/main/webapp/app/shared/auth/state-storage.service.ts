/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Injectable } from '@angular/core';
import { SessionStorageService } from 'ng2-webstorage';

@Injectable()
export class StateStorageService {
    constructor(
        private $sessionStorage: SessionStorageService
    ) {}

    getPreviousState() {
        return this.$sessionStorage.retrieve('previousState');
    }

    resetPreviousState() {
        this.$sessionStorage.clear('previousState');
    }

    storePreviousState(previousStateName, previousStateParams) {
        let previousState = { 'name': previousStateName, 'params': previousStateParams };
        this.$sessionStorage.store('previousState', previousState);
    }

    getDestinationState() {
        return this.$sessionStorage.retrieve('destinationState');
    }

    storeDestinationState(destinationState, destinationStateParams, fromState) {
        let destinationInfo = {
            'destination': {
                'name': destinationState.name,
                'data': destinationState.data,
            },
            'params': destinationStateParams,
            'from': {
                'name': fromState.name,
             }
        };
        this.$sessionStorage.store('destinationState', destinationInfo);
    }
}
