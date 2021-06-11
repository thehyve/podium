/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { AttachmentService } from '../attachment.service';
import { Attachment } from '../attachment.model';
import { AttachmentTypes } from '../attachment.constants';
import { User } from '../../user/user.model';
import { AccountService } from '../../../core/auth/account.service';
import { FormatHelper } from '../../util/format-helper';
import { Subscription } from 'rxjs';
import { RequestAccessService } from '../../request/request-access.service';
import { RequestBase } from '../../request/request-base';

const ATTACHMENT_TYPES = [
    {label: AttachmentTypes[AttachmentTypes.NONE], value: AttachmentTypes.NONE},
    {label: AttachmentTypes[AttachmentTypes.METC_LETTER], value: AttachmentTypes.METC_LETTER},
    {label: AttachmentTypes[AttachmentTypes.ORG_CONDITIONS], value: AttachmentTypes.ORG_CONDITIONS},
    {label: AttachmentTypes[AttachmentTypes.MTA], value: AttachmentTypes.MTA},
    {label: AttachmentTypes[AttachmentTypes.DTA], value: AttachmentTypes.DTA},
    {label: AttachmentTypes[AttachmentTypes.OTHER], value: AttachmentTypes.OTHER},
];

@Component({
    selector: 'pdm-attachment-list',
    templateUrl: './attachment-list.component.html',
    styleUrls: ['attachment-list.scss']
})
export class AttachmentListComponent implements OnChanges, OnInit, OnDestroy {

    account: User;
    accountSubscription: Subscription;
    attachmentTypes: any[];
    error: any[];

    @Input() request: RequestBase;
    @Input() attachments: Attachment[];
    @Input() canUpdate: boolean;

    @Output() onDeleteFile: EventEmitter<boolean>;
    @Output() onFileTypeChange: EventEmitter<Attachment>;

    static isFileOwner(user: User, attachment: Attachment): boolean {
        return attachment.owner && (user.uuid === attachment.owner.uuid);
    }

    constructor(private accountService: AccountService,
                private attachmentService: AttachmentService,
                private requestAccessService: RequestAccessService) {
        this.attachmentTypes = ATTACHMENT_TYPES;
        this.onDeleteFile = new EventEmitter<boolean>();
        this.onFileTypeChange = new EventEmitter<Attachment>();
        this.error = [];
    }

    ngOnInit(): void {
        this.accountSubscription = this.accountService.getAuthenticationState()
            .subscribe(
                (identity) => this.account = identity
            );
    }

    ngOnDestroy() {
        if (this.accountSubscription) {
            this.accountSubscription.unsubscribe();
        }
    }

    refreshError(attachments): void {
        this.error = [];
        if (attachments) {
            attachments.forEach((file: Attachment) => {
                if (file.requestFileType === AttachmentTypes.NONE) {
                    this.error.push({
                        filename: file.fileName
                    });
                }
            })
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.attachments) {
            let files = changes.attachments.currentValue;
            this.refreshError(files);
        }
    }

    onAttachmentTypeChange(attachment: Attachment, newType: AttachmentTypes) {
        attachment.requestFileType = newType;
        this.attachmentService.setAttachmentType(attachment).subscribe(
            () => {
                this.onFileTypeChange.emit(attachment);
            }
        );
    }

    deleteAttachment(attachment: Attachment) {
        let removeCall = this.attachmentService.remove(attachment);
        removeCall.subscribe(
            response => {
                if (response.status === 200) {
                    this.onDeleteFile.emit(true);
                }
            }
        );
    }

    downloadAttachment(attachment: Attachment): void {
        this.attachmentService.downloadAttachment(this.request, attachment.uuid).subscribe(
            (blob) => {
                // msSaveOrOpenBlob is for downloading on IE, the else statement for saving on FF/Chrome
                if (window.navigator.msSaveOrOpenBlob) {
                    window.navigator.msSaveOrOpenBlob(blob, attachment.fileName);
                } else {
                    let link = document.createElement('a');
                    link.href = window.URL.createObjectURL(blob);
                    link.download = attachment.fileName;
                    link.target = '_blank';
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                }
            }
        );
    }

    canEdit(attachment: Attachment): boolean {
        if (!this.canUpdate) {
            return false;
        }
        return AttachmentListComponent.isFileOwner(this.account, attachment) ||
            ((!attachment.owner) && this.requestAccessService.isCoordinatorFor(attachment.request));
    }

    formatBytes(bytes: number, precision: number) {
        return FormatHelper.formatBytes(bytes, precision);
    }

}
