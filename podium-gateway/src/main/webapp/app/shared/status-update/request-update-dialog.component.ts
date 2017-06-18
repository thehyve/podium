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
import { RequestStatusUpdateAction } from './request-update-action';
import { RequestReviewDecision } from '../request/request-review-decision';

@Component({

})

export class RequestUpdateDialogComponent {

    request: RequestBase;

    constructor(
        protected jhiLanguageService: JhiLanguageService,
        protected requestService: RequestService,
        protected activeModal: NgbActiveModal
    ) {

    }

    close() {
        this.activeModal.close('closed');
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

    applyStyles(status: any) {
        let style = {
            headerStyle: 'revision-header',
            buttonStyle: 'btn-default'
        };
        if (status === RequestStatusUpdateAction[RequestStatusUpdateAction.Reject] ||
            status === RequestReviewDecision[RequestReviewDecision.Rejected]) {
            style.headerStyle = 'reject-header';
            style.buttonStyle = 'btn-danger';
        }

        if (status === RequestStatusUpdateAction[RequestStatusUpdateAction.Close]) {
            style.headerStyle = 'close-header';
            style.buttonStyle = 'btn-danger';
        }

        return style;
    }
}
