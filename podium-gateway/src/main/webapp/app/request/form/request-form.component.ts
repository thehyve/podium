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
import { Organisation, OrganisationService } from '../../entities/';
import { RequestFormService } from './request-form.service';
import {
    RequestDetail,
    RequestType,
    PrincipalInvestigator,
    AttachmentService,
    RequestBase,
    RequestService,
    Principal,
    User,
    Attachment,
    EmailValidatorDirective
} from '../../shared';

@Component({
    selector: 'pdm-request-form',
    templateUrl: './request-form.component.html',
    styleUrls: ['request-form.scss']
})
export class RequestFormComponent implements OnInit {

    private currentUser: User;

    public requestFormDisabled: boolean;
    public error: string;
    public success: string;
    public requestBase: RequestBase;
    public requestDetail?: RequestDetail;
    public requestTypes = RequestType;
    public availableOrganisations: Organisation[];
    public availableRequestDrafts: RequestBase[];
    public selectDraft: boolean;
    public selectedDraft: any = null;
    public requestDraftsAvailable: boolean;
    public selectedRequestDraft: RequestBase;

    attachments: Attachment[];

    constructor(private jhiLanguageService: JhiLanguageService,
                private requestFormService: RequestFormService,
                private requestService: RequestService,
                private attachmentService: AttachmentService,
                private route: ActivatedRoute,
                private router: Router,
                private principal: Principal,
                private eventManager: EventManager,
                private organisationService: OrganisationService) {
        this.jhiLanguageService.setLocations(['request']);
    }

    ngOnInit() {
        this.principal.identity().then((account) => {
            this.currentUser = account;
            this.initializeRequestForm();
        });

        /**
         * Organisation resolve
         */
        this.organisationService.findAvailable().map((availableOrganisations) => {
            // TODO display list available organisations
        });
    }

    ngAfterContentInit() {
        this.registerChangeInFilesUploaded();
    }

    initializeRequestForm() {
        let uuid = this.currentUser.uuid;
        this.requestService.findAvailableRequestDrafts(uuid)
            .subscribe(
                (requestDrafts) => this.processAvailableDrafts(requestDrafts),
                (error) => this.onError('Error loading available request drafts.')
            );
    }

    registerChangeInFilesUploaded() {
        this.eventManager.subscribe('uploadListModification', (response) => this.loadAttachmentsForRequest());
    }

    loadAttachmentsForRequest() {
        this.attachmentService
            .findAttachmentsForRequest(this.requestBase.uuid)
            .subscribe(
                (attachments) => this.attachments = attachments,
                (error) => this.onError(error)
            );
    }

    processAvailableDrafts(requestDrafts) {
        this.selectDraft = true;
        this.availableRequestDrafts = requestDrafts;
        this.requestDraftsAvailable = true;
    }

    initializeBaseRequest() {
        let uuid = this.currentUser.uuid;
        this.requestService.initRequestBase(uuid)
            .subscribe(
                (requestBase) => {
                    this.selectedDraft = requestBase;
                    this.requestBase = requestBase;
                    this.requestDetail = requestBase.requestDetail;
                    this.selectDraft = false;
                },
                (error) => this.onError('Error initializing base request')
            );
    }

    selectRequestDraft(requestBase: RequestBase) {
        this.selectDraft = false;
        this.requestBase = requestBase;
        this.requestDetail = requestBase.requestDetail || new RequestDetail();
        this.requestDetail.principalInvestigator = requestBase.requestDetail.principalInvestigator || new PrincipalInvestigator();
    }

    saveRequestDraft() {
        this.requestBase.requestDetail = this.requestDetail;
        this.requestBase.requestDetail.principalInvestigator = this.requestDetail.principalInvestigator;
        this.requestService.saveRequestDraft(this.requestBase)
            .subscribe(
                (requestBase) => this.postSaveUpdate(requestBase),
                (error) => this.onError(error)
            );
    }

    private postSaveUpdate(requestBase: RequestBase) {
        // TODO
    }

    private onError(error) {
        this.error =  'ERROR';
       // TODO
    }

}
