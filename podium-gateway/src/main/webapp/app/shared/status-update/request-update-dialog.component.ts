/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import { RequestService } from '../request/request.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { RequestBase } from '../request/request-base';
import { Response } from '@angular/http';

@Component({

})

export class RequestUpdateDialogComponent {

    request: RequestBase;
    status: string;

    constructor(protected jhiLanguageService: JhiLanguageService,
                protected requestService: RequestService,
                protected activeModal: NgbActiveModal) {
        this.jhiLanguageService.setLocations(['request', 'requestStatus']);
    }

    close() {
        this.activeModal.dismiss('closed');
    }

    onError(err: string) {
        this.activeModal.dismiss(err);
    }

    onSuccess(res: Response) {
        this.request = res.json();
        this.requestService.requestUpdateEvent(this.request);
        this.activeModal.close();
    }

    onUnknownStatus() {
        this.activeModal.dismiss(new Error('Unknown status update action'));
    }
}
