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
import { JhiLanguageService } from 'ng-jhipster';
import { RequestService } from '../request';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PodiumEventMessage } from '../event/podium-event-message';
import { RequestUpdateDialogComponent } from './request-update-dialog.component';
import { RequestUpdateAction } from './request-update-action';

@Component({
    templateUrl: './request-update-dialog.component.html',
    styleUrls: ['request-update-dialog.scss']
})

export class RequestUpdateStatusDialogComponent extends RequestUpdateDialogComponent implements OnInit {
    statusUpdateAction: RequestUpdateAction;
    headerStyle: string;
    buttonStyle: string;
    status: string;
    public message: PodiumEventMessage = new PodiumEventMessage();

    constructor(protected jhiLanguageService: JhiLanguageService,
                protected requestService: RequestService,
                protected activeModal: NgbActiveModal) {
        super(jhiLanguageService, requestService, activeModal);
    }

    ngOnInit() {
        this.applyStyles();
    }

    applyStyles() {
        this.status = RequestUpdateAction[this.statusUpdateAction];
        if (this.status === RequestUpdateAction[RequestUpdateAction.Reject]) {
            this.headerStyle = 'reject-header';
            this.buttonStyle = 'btn-danger';
        } else {
            this.headerStyle = 'revision-header';
            this.buttonStyle = 'btn-default';
        }
    }

    close() {
        super.close();
    }

    /**
     * Confirm and submit a status update with a message
     * returns an unsubscribed observable with the action
     */
    confirmStatusUpdate() {
        if (this.statusUpdateAction === RequestUpdateAction.Reject) {
            this.requestService.rejectRequest(this.request.uuid, this.message)
                .subscribe((res) => this.onSuccess(res));

        }

        if (this.statusUpdateAction === RequestUpdateAction.Revision) {
            this.requestService.requestRevision(this.request.uuid, this.message)
                .subscribe((res) => this.onSuccess(res));
        }

        super.onUnknownStatus();
    }
}
