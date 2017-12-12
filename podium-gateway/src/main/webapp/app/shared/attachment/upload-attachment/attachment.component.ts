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
import { UploadOutput, UploadInput, UploadFile, humanizeBytes, UploaderOptions } from 'ngx-uploader';
import { AuthServerProvider } from '../../auth/auth-jwt.service';
import { AttachmentsService } from '../attachments.service';

@Component({
    selector: 'pdm-attachment',
    templateUrl: './attachment.component.html',
    styleUrls: ['attachment.scss']
})
export class AttachmentComponent {

    @Input() requestBaseId: string;
    @Output() onFinishedUpload: EventEmitter<boolean>;

    options: UploaderOptions;
    files: UploadFile[];
    uploadInput: EventEmitter<UploadInput>;
    humanizeBytes: Function;
    dragOver: boolean;
    maxFileSize: string;
    error: any[];

    constructor(private authServerProvider: AuthServerProvider,
                private attachmentService: AttachmentsService) {
        this.files = []; // local uploading files array
        this.uploadInput = new EventEmitter<UploadInput>(); // input events, we use this to emit data to ngx-uploader
        this.onFinishedUpload = new EventEmitter<boolean>();
        this.humanizeBytes = humanizeBytes;
        this.error = [];
        this.maxFileSize = null;
    }

    onUploadOutput(output: UploadOutput): void {
        let token = this.authServerProvider.getToken();
        if (output.type === 'allAddedToQueue') { // when all files added in queue
            // uncomment this if you want to auto upload files when added
            const event: UploadInput = {
                type: 'uploadAll',
                url: '/api/requests/' + this.requestBaseId + '/files',
                headers: {'Authorization': 'Bearer ' + token},
                method: 'POST'
            };
            this.error = [];
            this.uploadInput.emit(event);
        } else if (output.type === 'addedToQueue' && typeof output.file !== 'undefined') { // add file to array when added
            this.files.push(output.file);
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
            this.dragOver = false;
        } else if (output.type === 'done') {
            this.files = [];
            if (output.file.responseStatus >= 300) {
                this.error.push(output.file.response.message);
                this.onFinishedUpload.emit(false);
            } else {
                this.onFinishedUpload.emit(true);
            }
        }
    }

    startUpload(): void {
        const event: UploadInput = {
            type: 'uploadAll',
            url: '/api/requests/' + this.requestBaseId + '/addfile',
            method: 'POST'
        };

        this.uploadInput.emit(event);
    }

    cancelUpload(id: string): void {
        this.uploadInput.emit({type: 'cancel', id: id});
    }

    removeFile(id: string): void {
        this.uploadInput.emit({type: 'remove', id: id});
    }

    removeAllFiles(): void {
        this.uploadInput.emit({type: 'removeAll'});
    }

}
