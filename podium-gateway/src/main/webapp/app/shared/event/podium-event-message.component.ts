/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, Input } from '@angular/core';
import { RequestService } from '../request/request.service';
import { RequestBase } from '../request/request-base';

@Component({
    selector: 'pdm-event-message-component',
    templateUrl: 'podium-event-message.component.html'
})

export class PodiumEventMessageComponent implements OnInit {
    @Input()
    request: RequestBase;

    constructor(
        private requestService: RequestService
    ) {
        this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
        });
    }

    ngOnInit() {

    }

}
