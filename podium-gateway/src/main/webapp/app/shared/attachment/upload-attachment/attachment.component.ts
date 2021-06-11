/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { UploadOutput, UploadFile, UploadInput, humanizeBytes, UploaderOptions } from 'ngx-uploader';
import { MAX_UPLOAD_SIZE } from '../attachment.constants';
import { RequestBase } from '../../request/request-base';
import { AttachmentService } from '../attachment.service';

@Component({
    selector: 'pdm-attachment',
    templateUrl: './attachment.component.html',
    styleUrls: ['attachment.scss']
})
export class AttachmentComponent {

    @Input() request: RequestBase;
    @Output() onFinishedUpload: EventEmitter<boolean>;

    // input events, we use this to emit data to ngx-uploader
    uploadInput = new EventEmitter<UploadInput>();
    options: UploaderOptions;
    files: UploadFile[];
    humanizeBytes: any;
    dragOver: boolean;
    error: any[];

    constructor(private attachmentService: AttachmentService) {
        this.files = []; // local uploading files array
        this.onFinishedUpload = new EventEmitter<boolean>();
        this.humanizeBytes = humanizeBytes;
        this.error = [];
    }

    onUploadOutput(output: UploadOutput): void {
        if (output.type === 'allAddedToQueue') { // when all files added in queue
            this.attachmentService.uploadRequestFile(this.request, this.uploadInput);
        } else if (output.type === 'addedToQueue' && typeof output.file !== 'undefined') { // add file to array when added
            if (output.file.size > MAX_UPLOAD_SIZE) {
                this.error.push(`${output.file.name} is too big. Max attachment size is  ${MAX_UPLOAD_SIZE}`);
            } else {
                this.files.push(output.file);
            }
        } else if (output.type === 'uploading' && typeof output.file !== 'undefined') {
            // update current data in files array for uploading file
            const index = this.files.findIndex(file => typeof output.file !== 'undefined' && file.id === output.file.id);
            this.files[index] = output.file;
        } else if (output.type === 'removed') {
            // remove file from array when removed
            this.files = this.files.filter((file: UploadFile) => file !== output.file);
        } else if (output.type === 'dragOver') {
            this.dragOver = true;
        } else if (output.type === 'dragOut') {
            this.dragOver = false;
        } else if (output.type === 'drop') {
            this.error = [];
            this.dragOver = false;
        } else if (output.type === 'done') {
            this.files = [];
            this.onFinishedUpload.emit(output.file.responseStatus < 300);
        }
    }

}
