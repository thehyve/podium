/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, AfterContentInit, ViewChild, Input } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { EventManager } from 'ng-jhipster';
import { RequestFormService } from './request-form.service';
import {
    RequestDetail,
    RequestType,
    PrincipalInvestigator,
    RequestBase,
    RequestService,
    Principal,
    User
} from '../../shared';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { RequestFormSubmitDialogComponent } from './request-form-submit-dialog.component';
import { OrganisationSelectorComponent } from '../../shared/organisation-selector/organisation-selector.component';
import { RequestAccessService } from '../../shared/request/request-access.service';
import {
    RequestReviewStatusOptions,
    RequestOverviewStatusOption
} from '../../shared/request/request-status/request-status.constants';
import { OrganisationService } from '../../shared/organisation/organisation.service';
import { Organisation } from '../../shared/organisation/organisation.model';
import { equalParamsAndUrlSegments } from '@angular/router/src/router_state';

@Component({
    selector: 'pdm-request-form',
    templateUrl: './request-form.component.html',
    styleUrls: ['request-form.scss']
})

export class RequestFormComponent implements OnInit {

    private currentUser: User;

    @ViewChild(OrganisationSelectorComponent)
    private organisationSelectorComponent: OrganisationSelectorComponent;

    @Input() isInRevision: boolean;

    public error: string;
    public success: string;
    public requestBase: RequestBase;
    public requestDetail?: RequestDetail;
    public requestTypeOptions: any;
    public availableRequestDrafts: RequestBase[];
    public selectDraft: boolean;
    public selectedDraft: any = null;
    public requestDraftsAvailable: boolean;
    private revisionId: string;
    public isUpdating = false;

    constructor(
        private requestFormService: RequestFormService,
        private requestAccessService: RequestAccessService,
        private requestService: RequestService,
        private router: Router,
        private activatedRoute: ActivatedRoute,
        private principal: Principal,
        private modalService: NgbModal
    ) {
        this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.selectRequest(request);
        });
    }

    ngOnInit() {
        this.principal.identity().then((account) => {
            this.currentUser = account;
            this.requestTypeOptions = RequestType;
            this.initializeRequestForm();
        });
    }

    initializeRequestForm() {
        if (this.router.url === '/requests/new'  && !this.isInRevision) {
            this.initializeBaseRequest();
        } else {
            this.activatedRoute.paramMap
                .switchMap((params: ParamMap) => this.requestService.findByUuid(params.get('uuid')))
                .subscribe(
                    request => {
                        this.requestFormService.request = request;
                        this.selectRequest(this.requestFormService.request);
                    },
                    error => {
                        this.onError(error);
                        this.router.navigate(['404'])
                    }
                );
        }

    }

    hasSelectedMultipleOrganisations() {
        return this.requestBase.organisations.length > 1;
    }

    initializeBaseRequest() {
        this.requestService.createDraft()
            .subscribe(
                (requestBase) => {
                    this.selectedDraft = requestBase;
                    this.requestBase = requestBase;
                    this.requestBase.organisations = requestBase.organisations || [];
                    this.requestDetail = requestBase.requestDetail;
                    this.requestDetail.requestType = requestBase.requestDetail.requestType || [];
                },
                (error) => this.onError('Error initializing base request')
            );
    }

    updateRequestOrganisations(organisations: Organisation[]) {
        this.requestBase.organisations = organisations;
        if (!this.hasSelectedMultipleOrganisations()) {
            this.requestDetail.combinedRequest = false;
        }
    }

    selectRequest(requestBase: RequestBase) {
        this.requestBase = requestBase;
        this.requestBase.organisations = requestBase.organisations || [];

        // If the request is in revision use the revisionDetail
        if (this.isRequesterOfRevisionRequest(requestBase)) {
            // Remember the revision ID
            this.revisionId = requestBase.revisionDetail.id;
            this.requestDetail = requestBase.revisionDetail;
        } else {
            this.requestDetail = requestBase.requestDetail || new RequestDetail();
        }

        this.requestDetail.requestType = requestBase.requestDetail.requestType || [];
    }

    isRequesterOfRevisionRequest(requestBase: RequestBase): boolean {
        return this.requestAccessService.isRequesterOf(requestBase)
            && RequestAccessService.isRequestStatus(requestBase, RequestOverviewStatusOption.Revision);
    }

    updateRequestType(selectedRequestType, event) {
        let _idx = this.requestDetail.requestType.indexOf(selectedRequestType.value);
        if ( _idx < 0) {
            this.requestDetail.requestType.push(selectedRequestType.value);
        } else {
            this.requestDetail.requestType.splice(_idx, 1);
        }
        this.organisationSelectorComponent.filterOptionsByRequestType();
    }

    saveRequestDraft() {
        this.isUpdating = true;
        this.requestBase.requestDetail = this.requestDetail;
        this.requestBase.requestDetail.principalInvestigator = this.requestDetail.principalInvestigator;
        this.requestService.saveDraft(this.requestBase)
            .subscribe(
                (requestBase) => this.onSuccess(requestBase),
                (error) => this.onError(error)
            );
    }

    confirmSubmitModal(request: RequestBase) {
        let modalRef = this.modalService.open(RequestFormSubmitDialogComponent, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.request = request;
        modalRef.result.then(result => {
            console.log(`Closed with: ${result}`);
            this.isUpdating = false;
        }, (reason) => {
            console.log(`Dismissed ${reason}`);
            this.isUpdating = false;
        });
    }

    submitDraft() {
        this.isUpdating = true;
        this.requestBase.requestDetail = this.requestDetail;
        this.requestBase.requestDetail.principalInvestigator = this.requestDetail.principalInvestigator;

        this.requestService.saveDraft(this.requestBase)
            .subscribe(
                (request) => this.confirmSubmitModal(request),
                (error) => this.onError(error)
            );
    }

    /**
     * Save a temporary version of a request
     */
    saveRequest() {
        this.isUpdating = true;
        this.requestBase.revisionDetail = this.requestDetail;
        this.requestBase.revisionDetail.principalInvestigator = this.requestDetail.principalInvestigator;
        this.requestBase.revisionDetail.id = this.revisionId;

        this.requestService.saveRequestRevision(this.requestBase)
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    submitRequest() {
        this.isUpdating = true;
        this.requestBase.requestDetail = this.requestDetail;
        this.requestBase.requestDetail.principalInvestigator = this.requestDetail.principalInvestigator;
        this.requestService.saveRequestRevision(this.requestBase)
            // Submit the request
            .flatMap(() => this.requestService.submitRequestRevision(this.requestBase.uuid))
            .subscribe(
                (res) => this.onSuccess(res),
                (err) => this.onError(err)
            );
    }

    /**
     * Return to the request overview
     */
    cancel() {
        return this.router.navigate(['/requests/my-requests']);
    }

    /**
     * Reset the requestDetails and principal investigator details.
     * The entity id's are remembered and restored.
     */
    reset() {
        // Remember the id's
        let requestDetailId = this.requestDetail.id;
        let principalInvestigatorId = this.requestDetail.principalInvestigator.id;
        this.requestDetail = new RequestDetail();
        this.requestDetail.principalInvestigator = new PrincipalInvestigator();

        // Restore the id's
        this.requestDetail.id = requestDetailId;
        this.requestDetail.principalInvestigator.id = principalInvestigatorId;
    }

    private onSuccess(result) {
        this.isUpdating = false;
        this.error =  null;
        this.success = 'SUCCESS';
        window.scrollTo(0, 0);

        this.requestService.requestUpdateEvent(result);
    }

    private onError(error) {
        this.isUpdating = false;
        this.error =  'ERROR';
        this.success = null;
        window.scrollTo(0, 0);
    }

}
