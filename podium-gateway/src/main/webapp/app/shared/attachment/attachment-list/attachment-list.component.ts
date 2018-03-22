/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { AttachmentsService } from '../attachments.service';
import { Attachment } from '../attachment.model';
import { AttachmentTypes } from '../attachment.constants';
import { Principal, User } from '../../';
import { FormatHelper } from '../../util/format-helper';

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
export class AttachmentListComponent implements OnChanges, OnInit {

    currentAccount: any;
    attachmentTypes: any[];
    error: any[];

    @Input() requestUUID: string;
    @Input() attachments: Attachment[];
    @Input() canUpdate: boolean;

    @Output() onDeleteFile: EventEmitter<boolean>;
    @Output() onFileTypeChange: EventEmitter<Attachment>;

    constructor(private principal: Principal,
                private attachmentService: AttachmentsService) {
        this.attachmentTypes = ATTACHMENT_TYPES;
        this.onDeleteFile = new EventEmitter<boolean>();
        this.onFileTypeChange = new EventEmitter<Attachment>();
        this.error = [];
    }

    static isFileOwner(user: User, attachment: Attachment): boolean {
        return user.uuid === attachment.owner.uuid;
    }

    ngOnInit(): void {
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
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
        let files = changes.attachments.currentValue;
        this.refreshError(files);
    }

    onAttachmentTypeChange(attachment: Attachment, newType: AttachmentTypes) {
        attachment.requestFileType = newType;
        this.attachmentService.setAttachmentType(attachment).subscribe(
            response => {
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
        this.attachmentService.downloadAttachment(this.requestUUID, attachment.uuid).subscribe(
            (blob) => {
                let link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);
                link.download = attachment.fileName;
                link.click();
            }
        );
    }

    canEdit(attachment: Attachment): boolean {
        return attachment ?
            AttachmentListComponent.isFileOwner(this.currentAccount, attachment) && this.canUpdate : this.canUpdate;
    }

    formatBytes(bytes: number, precision: number) {
        return FormatHelper.formatBytes(bytes, precision);
    }

}
