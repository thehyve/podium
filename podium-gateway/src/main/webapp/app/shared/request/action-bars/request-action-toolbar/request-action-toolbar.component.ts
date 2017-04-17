/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import { Form } from '@angular/forms';
import { RequestBase } from '../../request-base';
import { RequestStatus } from '../../request-status';

@Component({
    selector: 'pdm-request-action-toolbar',
    templateUrl: './request-action-toolbar.component.html',
    styleUrls: ['request-action-toolbar.scss']
})

export class RequestActionToolbarComponent implements OnInit {

    private status: RequestStatus;
    public requestStatus = RequestStatus;

    @Input() form: Form;
    @Input() request: RequestBase;

    @Output() resetChange = new EventEmitter();
    @Output() rejectChange = new EventEmitter();
    @Output() saveDraftChange = new EventEmitter();
    @Output() submitDraftChange = new EventEmitter();
    @Output() validateRequestChange = new EventEmitter();
    @Output() requireRevisionChange = new EventEmitter();

    constructor(private jhiLanguageService: JhiLanguageService) {
        this.jhiLanguageService.setLocations(['request']);
    }

    ngOnInit() {
        this.status = this.request.status;
    }

    isStatus(status): boolean {
        return this.status === status;
    }

    saveDraft() {
        this.saveDraftChange.emit(true);
    }

    submitDraft() {
        this.submitDraftChange.emit(true);
    }

    resetForm() {
        this.resetChange.emit(true);
    }

    rejectRequest() {
        this.rejectChange.emit(true);
    }

    validateRequest() {
        this.validateRequestChange.emit(true);
    }

    requireRevision() {
        this.requireRevisionChange.emit(true);
    }

}
