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
import { ActivatedRoute, Router } from '@angular/router';
import { JhiLanguageService, EventManager } from 'ng-jhipster';
import { RequestDetail } from '../../shared/request/request-detail';
import { RequestFormService } from './request-form.service';
import { RequestType } from '../../shared/request/request-type';
import { Organisation, OrganisationService } from '../../entities/';
import { AttachmentService } from '../../shared/attachment/attachment.service';
import { RequestBase } from '../../shared/request/request-base';
import { RequestService } from '../../shared/request/request.service';
import { Principal } from '../../shared/auth/principal.service';
import { User } from '../../shared/user/user.model';
import { Attachment } from '../../shared/attachment/attachment';

@Component({
    selector: 'pdm-request-form',
    templateUrl: './request-form.component.html',
    styleUrls: ['request-form.scss']
})
export class RequestFormComponent implements OnInit {

    public requestFormDisabled: boolean;
    private currentUser: User;
    public error: string;
    public success: string;

    public requestBase: RequestBase;
    public request?: RequestDetail;
    public requestTypes = RequestType;

    public availableOrganisations: Organisation[];
    public availableRequestDrafts: RequestBase[];
    public selectDraft: boolean;
    public selectedDraft: any = null;
    public requestDraftsAvailable: boolean;
    public selectedRequestDraft: RequestBase;

    attachments: Attachment[];

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private requestFormService: RequestFormService,
        private requestService: RequestService,
        private attachmentService: AttachmentService,
        private route: ActivatedRoute,
        private router: Router,
        private principal: Principal,
        private eventManager: EventManager,
        private organisationService: OrganisationService
    ) {
        this.jhiLanguageService.setLocations(['request']);
    }

    ngOnInit () {
        this.principal.identity().then((account) => {
            console.log('Got user ', account);
            this.currentUser = account;
            this.initializeRequestForm();
        });

        /**
         * Organisation resolve
         */
        this.organisationService.findAvailable().map((availableOrganisations) => {

        });

        /**
         * Resolve Tags
         */
    }

    ngAfterContentInit () {
        this.registerChangeInFilesUploaded();
    }

    initializeRequestForm () {
        // Resolve Draft Requests
        let uuid = this.currentUser.uuid;
        this.requestService.findAvailableRequestDrafts(uuid)
            .subscribe(
                (requestDrafts) => this.processAvailableDrafts(requestDrafts),
                (error) => this.onError('Error loading available request drafts.')
            );
    }

    registerChangeInFilesUploaded () {
        this.eventManager.subscribe('uploadListModification', (response) => this.loadAttachmentsForRequest());
    }

    loadAttachmentsForRequest () {
        this.attachmentService
            .findAttachmentsForRequest(this.requestBase.uuid)
            .subscribe(
                (attachments) => this.attachments = attachments,
                (error) => this.onError(error)
            );
    }

    processAvailableDrafts(requestDrafts) {
        if (!requestDrafts.length) {
            this.selectDraft = false;
            return this.initializeBaseRequest();
        }

        this.selectDraft = true;
        this.availableRequestDrafts = requestDrafts;
        this.requestDraftsAvailable = true;
    }

    initializeBaseRequest() {
        let uuid = this.currentUser.uuid;
        this.requestService.initRequestBase(uuid)
            .subscribe(
                (requestBase) => {
                    this.requestBase = requestBase;
                    this.request = new RequestDetail();
                    this.selectDraft = false;
                },
                (error) => this.onError('Error initializing base request')
            );
    }

    selectRequestDraft (requestBase: RequestBase) {
        this.selectDraft = false;
        this.requestBase = requestBase;
        this.request = requestBase.detail || new RequestDetail();
    }

    saveRequestDraft () {
        this.requestService.saveRequestDraft(this.requestBase)
            .subscribe(
                (requestBase) => this.postSaveUpdate(requestBase),
                (error) => this.onError(error)
            );
    }

    private postSaveUpdate (requestBase: RequestBase) {
    }

    onError(error) {
        console.warn('An error occurred ', error);
    }

}
