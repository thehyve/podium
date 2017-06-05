/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';
import { RequestService } from '../../shared/request/request.service';
import { RequestBase } from '../../shared/request/request-base';
import { MessageService } from '../../shared/message/message.service';
import { Message } from '../../shared/message/message.model';

@Component({
    selector: 'request-form-submit-dialog',
    templateUrl: './request-form-submit-dialog.component.html'
})
export class RequestFormSubmitDialogComponent {

    request: RequestBase;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private requestService: RequestService,
        public activeModal: NgbActiveModal,
        private router: Router,
        private messageService: MessageService) {
        this.jhiLanguageService.setLocations(['request']);
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    setSubmitSuccessMessage(requests: RequestBase[]) {
        let submittedTitle = `The request has been successfully submitted.`;
        let submittedMessage = `<ul>`;
        for (let req of requests) {
            for (let organisation of req.organisations) {
                submittedMessage += `<li>Request for organisation ${organisation.name}.</li>`;
            }
        }
        submittedMessage += `</ul>`;
        this.messageService.store(new Message(submittedTitle, submittedMessage));
    }

    confirmSubmit(request: RequestBase) {
        this.requestService.submitDraft(request.uuid).subscribe(response => {
            this.activeModal.dismiss(true);
            this.setSubmitSuccessMessage(response);
            this.router.navigate(['completed', { outlets: { submit: null } }], { replaceUrl: true });
        });
    }

}
