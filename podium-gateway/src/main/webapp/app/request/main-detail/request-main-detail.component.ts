/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { AfterViewInit, Component, ViewChild, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlertService } from '../../core/util/alert.service';
import { RequestBase } from '../../shared/request/request-base';
import { RequestService } from '../../shared/request/request.service';
import { RequestDetailComponent } from './detail/request-detail.component';
import { Attachment } from '../../shared/attachment/attachment.model';
import { AttachmentService } from '../../shared/attachment/attachment.service';
import { RequestAccessService } from '../../shared/request/request-access.service';
import { RequestOverviewStatusOption } from '../../shared/request/request-status/request-status.constants';

@Component({
    selector: 'pdm-request-main-detail',
    templateUrl: './request-main-detail.component.html',
    styleUrls: ['request-main-detail.scss'],
    encapsulation: ViewEncapsulation.None
})

export class RequestMainDetailComponent implements AfterViewInit {

    /**
     * Setup component as ViewChild to access methods inside child.
     * Used for review and method accessors in sibling components
     */
    @ViewChild(RequestDetailComponent)
    private requestDetail: RequestDetailComponent;

    public _request: RequestBase;
    public requestShortName = '';
    public asterisk = '';
    public attachments: Attachment[];

    public error: any;
    public success: any;

    constructor(
        private route: ActivatedRoute,
        private requestService: RequestService,
        private alertService: AlertService,
        private attachmentService: AttachmentService,
        private requestAccessService: RequestAccessService
    ) {

        this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
        });
    }

    set request(request) {
        this._request = request;
        this.requestShortName = request.organisations[0].shortName.replace(/"/g, '\\"');
    }

    get request() {
        return this._request;
    }

    ngAfterViewInit() {
        this.attachments = [];
        this.route.data
            .subscribe((data: { request: RequestBase }) => {
                this.request = data.request;
                this.getAttachments(data.request);
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

    private getAttachments (request: RequestBase) {
        this.attachmentService.getAttachments(request).subscribe(
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
            this.getAttachments(this.request);
        }
    }

    onDeleteAttachment(isSuccess: boolean) {
        if (isSuccess) {
            this.getAttachments(this.request);
        }
    }

    onAttachmentTypeChange(attachment: Attachment) {
        if (attachment) {
            this.getAttachments(this.request);
        }
    }

    /**
     * User can change attachments when:
     * - Has researcher role and when request is still a draft or in revision
     * - Has coordinator role and when request is in validation or in review
     * @returns {boolean}
     */
    canChangeAttachments() {
        if (!this.request) {
            return false;
        }
        let isRequester = this.requestAccessService.isRequesterOf(this.request);
        let isCoordinator = this.requestAccessService.isCoordinatorFor(this.request);
        let isInRevision = this.isInRevision();
        let isInValidation = RequestAccessService.isRequestStatus(this.request, RequestOverviewStatusOption.Validation);
        let isInReview = RequestAccessService.isRequestStatus(this.request, RequestOverviewStatusOption.Review);
        return (isInRevision && isRequester) || (isCoordinator && isInValidation) || (isCoordinator && isInReview);
    }

    canViewAttachmentTab() {
        if (!this.request) {
            return false;
        }
        return !this.isInRevision() || this.requestAccessService.isCoordinatorFor(this.request);
    }

    isInRevision() {
        return  RequestAccessService.isRequestStatus(this.request, RequestOverviewStatusOption.Revision);
    }

}
