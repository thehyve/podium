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
import { RequestStatusUpdateAction } from './request-status-update-action';
import { PodiumEventMessage } from '../event/podium-event-message';
import { RequestBase, RequestService } from '../request';
import { Response } from '@angular/http';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'pdm-request-status-update',
    templateUrl: './request-status-update.component.html',
    styleUrls: ['request-status-update.scss']
})

export class RequestStatusUpdateDialogComponent implements OnInit {

    request: RequestBase;
    statusUpdateAction: RequestStatusUpdateAction;
    status: string;
    message: PodiumEventMessage = new PodiumEventMessage();

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private requestService: RequestService,
        private activeModal: NgbActiveModal) {
        this.jhiLanguageService.setLocations(['request', 'requestStatus']);
    }

    ngOnInit() {
        this.status = RequestStatusUpdateAction[this.statusUpdateAction];
    }

    close() {
        this.activeModal.dismiss('closed');
    }

    /**
     * Confirm and submit a status update with a message
     *
     * returns an unsubscribed observable with the action
     */
    confirmStatusUpdate() {
        if (this.statusUpdateAction === RequestStatusUpdateAction.Reject) {
            this.requestService.rejectRequest(this.request.uuid, this.message)
                .subscribe((res) => this.onSuccess(res));

        }

        if (this.statusUpdateAction === RequestStatusUpdateAction.Revision) {
            this.requestService.requestRevision(this.request.uuid, this.message)
                .subscribe((res) => this.onSuccess(res));
        }

        this.activeModal.dismiss(new Error('Unknown status update action'));
    }

    onSuccess(res: Response) {
        this.request = res.json();
        this.requestService.requestUpdateEvent(this.request);
        this.activeModal.close();
    }
}
