/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, ViewChild, Input, OnDestroy } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { forkJoin, Observable, of, Subscription } from 'rxjs';
import { catchError, map, mergeMap, switchMap } from 'rxjs/operators';
import { RequestFormService } from './request-form.service';
import { RequestDetail } from '../../shared/request/request-detail';
import { RequestType } from '../../shared/request/request-type';
import { PrincipalInvestigator } from '../../shared/request/principal-investigator';
import { RequestBase } from '../../shared/request/request-base';
import { RequestService } from '../../shared/request/request.service';
import { User } from '../../shared/user/user.model';
import { AccountService } from '../../core/auth/account.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { RequestFormSubmitDialogComponent } from './request-form-submit-dialog.component';
import { OrganisationSelectorComponent } from '../../shared/organisation-selector/organisation-selector.component';
import { RequestAccessService } from '../../shared/request/request-access.service';
import { RequestOverviewStatusOption } from '../../shared/request/request-status/request-status.constants';
import { Organisation } from '../../shared/organisation/organisation.model';
import { Attachment } from '../../shared/attachment/attachment.model';
import { AttachmentService } from '../../shared/attachment/attachment.service';
import { AttachmentComponent } from '../../shared/attachment/upload-attachment/attachment.component';
import { AttachmentListComponent } from '../../shared/attachment/attachment-list/attachment-list.component';
import { NgForm } from '@angular/forms';
import { OrganisationService } from '../../shared/organisation/organisation.service';
import { RequestTemplate } from '../../shared/request/request-template';

@Component({
    selector: 'pdm-request-form',
    templateUrl: './request-form.component.html',
    styleUrls: ['request-form.scss']
})

export class RequestFormComponent implements OnInit, OnDestroy {

    private currentUser: User;

    @ViewChild('requestForm') requestForm: NgForm;

    @ViewChild(AttachmentComponent)
    private attachmentComponent: AttachmentComponent;

    @ViewChild(AttachmentListComponent)
    private attachmentListComponent: AttachmentListComponent;

    @ViewChild(OrganisationSelectorComponent)
    private organisationSelectorComponent: OrganisationSelectorComponent;

    @Input() isInRevision: boolean;

    public error: string;
    public listOfInvalidOrganisationUUID: string[];
    public success: string;
    public requestBase: RequestBase;
    public requestDetail?: RequestDetail;
    public requestTypeOptions: any;
    public selectedDraft: any = null;
    public isUpdating = false;
    public attachments: Attachment[];

    public templateUUID: string;

    public searchQuery: string;

    private revisionId: string;

    private requestUpdateSubscription: Subscription = null;

    constructor(private requestFormService: RequestFormService,
                private requestAccessService: RequestAccessService,
                private requestService: RequestService,
                private router: Router,
                private activatedRoute: ActivatedRoute,
                private accountService: AccountService,
                private modalService: NgbModal,
                private attachmentService: AttachmentService,
                private organisationService: OrganisationService) {
        this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.selectRequest(request);
            this.getAttachments(request);
        });
    }

    ngOnInit() {
        this.accountService.identity().subscribe((account) => {
            this.currentUser = account;
            this.requestTypeOptions = RequestType;
            this.initializeRequestForm();
        });
    }

    ngOnDestroy() {
        if (this.requestUpdateSubscription !== null) {
            this.requestUpdateSubscription.unsubscribe();
        }
    }

    onFinishedUploadAttachment(success: boolean) {
        if (success) {
            this.getAttachments(this.requestBase);
        }
    }

    onDeleteAttachment(isSuccess: boolean) {
        if (isSuccess) {
            this.getAttachments(this.requestBase);
        }
    }

    onAttachmentTypeChange(attachment: Attachment) {
        if (attachment) {
            this.getAttachments(this.requestBase);
        }
    }

    private getAttachments(request: RequestBase) {
        this.attachmentService.getAttachments(request).subscribe(
            (attachments) => {
                this.attachments = attachments;
                this.requestBase.hasAttachmentsTypes = !this.hasAttachmentsTypeNone();
            },
            (error) => {
                console.error(error)
            }
        );
    }

    private hasAttachmentsTypeNone(): boolean {
        return this.attachmentService.hasAttachmentsTypeNone(this.attachments);
    }

    initializeRequestForm() {
        if (this.router.url.substring(0, 13) === '/requests/new' && !this.isInRevision) {
            this.initializeBaseRequest();
        } else {
            this.requestUpdateSubscription = this.activatedRoute.paramMap
                .pipe(switchMap((params: ParamMap) => this.requestService.findByUuid(params.get('uuid'))))
                .subscribe(
                    request => {
                        this.requestFormService.request = request;
                        this.selectRequest(this.requestFormService.request);
                        this.getAttachments(this.requestFormService.request);
                    },
                    (err) => {
                        this.onError(err);
                        this.router.navigate(['404'])
                    }
                );
        }

    }

    hasSelectedMultipleOrganisations() {
        return this.requestBase.organisations.length > 1;
    }

    populateRequestDetails(requestTemplate: RequestTemplate) {

        // map search query
        this.requestDetail.searchQuery = requestTemplate.humanReadable;

        if (requestTemplate.organisations) {

            let organisationObservables: Observable<any>[] = [];
            this.listOfInvalidOrganisationUUID = [];

            // Select all types when organizations are passed
            this.requestDetail.requestType = [
                RequestType.Data, RequestType.Images, RequestType.Material
            ];

            // Get organisations by uuid
            for (let collection of requestTemplate.organisations) {
                let obx = this.organisationService.findByUuid(collection).pipe(
                    map((res: Organisation) => res),
                    catchError(() => {
                        this.listOfInvalidOrganisationUUID.push(collection);
                        return of({});
                    }));
                organisationObservables.push(obx);
            }

            // Display as selected organisation when uuids are matched
            forkJoin(organisationObservables).subscribe(
                dataArray => {
                    this.requestBase.organisations = dataArray.filter(obj => {
                        return Object.keys(obj).length > 0;
                    });
                    this.organisationSelectorComponent.organisations = this.requestBase.organisations;
                },
                () => {},
                () => {
                    // TODO: Display invalid uuids in error alert
                    if (this.listOfInvalidOrganisationUUID.length) {
                        console.error('Invalid organisation uuids', this.listOfInvalidOrganisationUUID);
                    }
                }
            );
        } else {
            this.requestDetail.requestType = this.requestBase.requestDetail.requestType || [];
        }
    }

    initializeBaseRequest() {
        this.requestService.createDraft()
            .subscribe(
                (requestBase) => {

                    this.selectedDraft = requestBase;
                    this.requestBase = requestBase;
                    this.requestDetail = requestBase.requestDetail;

                    this.activatedRoute.queryParams.subscribe(params => {
                        if ('template_uuid' in params) {
                            this.templateUUID = params['template_uuid'];
                            this.requestService.getTemplateByUuid(params['template_uuid'])
                                .subscribe(
                                    (requestTemplate) => this.populateRequestDetails(requestTemplate),
                                    (err) => this.onError(err)
                                )
                        }
                    });
                    this.getAttachments(requestBase);
                },
                () => this.onError('Error initializing base request')
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

    updateRequestType(selectedRequestType) {
        let _idx = this.requestDetail.requestType.indexOf(selectedRequestType.value);
        if (_idx < 0) {
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
        let modalRef = this.modalService.open(RequestFormSubmitDialogComponent, {size: 'lg', backdrop: 'static'});
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
            .pipe(mergeMap(() => this.requestService.submitRequestRevision(this.requestBase.uuid)))
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
        this.error = null;
        this.success = 'SUCCESS';
        window.scrollTo(0, 0);

        this.requestService.requestUpdateEvent(result);
    }

    private onError(err: any) {
        console.error(err);
        this.isUpdating = false;
        this.error = 'ERROR';
        this.success = null;
        window.scrollTo(0, 0);
    }

}
