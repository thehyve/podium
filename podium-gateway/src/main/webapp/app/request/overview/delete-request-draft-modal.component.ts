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
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { RequestService } from '../../shared/request/request.service';
import { EventManager } from 'ng-jhipster';
import { RequestBase } from '../../shared/request/request-base';

@Component({
    selector: 'pdm-delete-request-draft-modal',
    templateUrl: './delete-request-draft-modal.component.html'
})
export class RequestDraftModalModalComponent {

    request: RequestBase;

    constructor(
        private requestService: RequestService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager
    ) {}

    confirmDelete() {
        this.requestService.deleteDraft(this.request.uuid).subscribe((res) => {
            this.eventManager.broadcast({
                name: 'requestListModification',
                content: 'Deleted a request'
            });
            this.activeModal.close(res.ok);
        });
    }
}
