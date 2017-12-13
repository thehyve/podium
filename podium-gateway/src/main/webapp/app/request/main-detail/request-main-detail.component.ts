/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { JhiAlertService } from 'ng-jhipster';
import { RequestBase } from '../../shared/request/request-base';
import { ActivatedRoute } from '@angular/router';
import { RequestService } from '../../shared/request/request.service';
import { RequestDetailComponent } from './detail/request-detail.component';
import { Attachment } from '../../shared/attachment/attachment.model';
import { AttachmentsService } from '../../shared/attachment/attachments.service';
import { RequestAccessService } from '../../shared/request/request-access.service';
import { RequestOverviewStatusOption } from '../../shared/request/request-status/request-status.constants';

@Component({
    selector: 'pdm-request-main-detail',
    templateUrl: './request-main-detail.component.html',
    styleUrls: ['request-main-detail.scss'],
    encapsulation: ViewEncapsulation.None
})

export class RequestMainDetailComponent implements OnInit {

    /**
     * Setup component as ViewChild to access methods inside child.
     * Used for review and method accessors in sibling components
     */
    @ViewChild(RequestDetailComponent)
    private requestDetail: RequestDetailComponent;

    public request: RequestBase;
    public asterisk: string = '';
    public attachments: Attachment[];

    public error: any;
    public success: any;

    constructor(
        private route: ActivatedRoute,
        private requestService: RequestService,
        private alertService: JhiAlertService,
        private attachmentService: AttachmentsService,
        private requestAccessService: RequestAccessService
    ) {

        this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
        });
    }

    ngOnInit() {
        this.attachments = [];
        this.route.data
            .subscribe((data: { request: RequestBase }) => {
                this.request = data.request;
                this.getAttachments(data.request.uuid);
                this.onSuccess(data.request);
            }, err => this.onError(err));
    }

    private onSuccess(request: RequestBase) {
        this.request = request;
        this.requestDetail.setRequest(request);
    }

    private onError(error) {
        this.alertService.error(error.error, error.message, null);
        this.success = null;
    }

    private getAttachments (requestUUID) {
        this.attachmentService.getAttachments(requestUUID).subscribe(
            (attachments) => {
                this.attachments = attachments;
                this.request.hasAttachmentsTypes = !this.hasAttachmentsTypeNone();
                this.asterisk = this.hasAttachmentsTypeNone() ? '*' : '';
            },
            (error) => {
                console.error(error)
            }
        );
    }

    private hasAttachmentsTypeNone (): boolean {
        return this.attachmentService.hasAttachmentsTypeNone(this.attachments);
    }

    onFinishedUploadAttachment(success: boolean) {
        if (success) {
            this.getAttachments(this.request.uuid);
        }
    }

    onDeleteAttachment(isSuccess: boolean) {
        if (isSuccess) {
            this.getAttachments(this.request.uuid);
        }
    }

    onAttachmentTypeChange(attachment: Attachment) {
        if (attachment) {
            this.getAttachments(this.request.uuid);
        }
    }

    /**
     * User can change attachments when:
     * - Has researcher role and when request is still a draft or in revision
     * - Has coordinator role and when request is in validation or in review
     * @returns {boolean}
     */
    canChangeAttachments() {
        let isInRevision = RequestAccessService.isRequestStatus(this.request, RequestOverviewStatusOption.Revision);
        let isRequester = this.requestAccessService.isRequesterOf(this.request);
        let isCoordinator = this.requestAccessService.isCoordinatorFor(this.request);
        let isInValidation = RequestAccessService.isRequestStatus(this.request, RequestOverviewStatusOption.Validation);
        let isInReview = RequestAccessService.isRequestStatus(this.request, RequestOverviewStatusOption.Review);
        return (isInRevision && isRequester) || (isCoordinator && isInValidation) || (isCoordinator && isInReview);
    }

}
