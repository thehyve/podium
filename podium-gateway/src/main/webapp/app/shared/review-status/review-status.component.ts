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
import { RequestBase } from '../request/request-base';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';

@Component({
    selector: 'pdm-review-status',
    templateUrl: './review-status.component.html'
})

export class ReviewStatusComponent implements OnInit {

    request: RequestBase;

    constructor(private jhiLanguageService: JhiLanguageService,
                private activeModal: NgbActiveModal) {
        this.jhiLanguageService.setLocations(['request', 'requestStatus']);
    }


    ngOnInit(): void {
        console.log(this.request);
    }

    close(): void {
        this.activeModal.dismiss('closed');
    }
}
