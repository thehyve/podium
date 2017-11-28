/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AttachmentsService } from '../attachments.service';
import { Attachment } from '../attachment.model';
import { AttachmentTypes } from '../attachment.constants';

@Component({
    selector: 'pdm-attachment-list',
    templateUrl: './attachment-list.component.html',
    styleUrls: ['attachment-list.scss']
})
export class AttachmentListComponent {

    attachmentTypes: any[] = [
        {label: AttachmentTypes[AttachmentTypes.NOT_SET], value: AttachmentTypes.NOT_SET},
        {label: AttachmentTypes[AttachmentTypes.METC_LETTER], value: AttachmentTypes.METC_LETTER},
        {label: AttachmentTypes[AttachmentTypes.ORG_CONDITIONS], value: AttachmentTypes.ORG_CONDITIONS},
        {label: AttachmentTypes[AttachmentTypes.MTA], value: AttachmentTypes.MTA},
        {label: AttachmentTypes[AttachmentTypes.DTA], value: AttachmentTypes.DTA},
        {label: AttachmentTypes[AttachmentTypes.OTHER], value: AttachmentTypes.OTHER},
    ];

    @Input() requestUUID: string;
    @Input() attachments: Attachment[];
    @Output() onDeleteFile: EventEmitter<boolean>;

    constructor(private attachmentService: AttachmentsService) {
        this.onDeleteFile = new EventEmitter<boolean>();
    }

    deleteAttachment(attachment: Attachment) {
        let removeCall = this.attachmentService.remove(attachment);
        removeCall.subscribe(
            response => {
                if (response.toString() === 'true') {
                    this.onDeleteFile.emit(true);
                } else {
                    this.onDeleteFile.emit(false);
                }
            },
            error => {
                this.onDeleteFile.emit(false);
            }
        );
    }

    downloadAttachment(attachment: Attachment) {
        this.attachmentService.downloadAttachment(this.requestUUID, attachment.uuid).subscribe(
            (blob) => {
                let link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);
                link.download = attachment.fileName;
                link.click();
            },
            (error) => {
                console.error('error', error);
            }
        );
    }


    formatByte(bytes: any, precision: number) {
        if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
        if (typeof precision === 'undefined') precision = 1;
        const units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'],
            number = Math.floor(Math.log(bytes) / Math.log(1024));
        return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) + ' ' + units[number];
    }
}
