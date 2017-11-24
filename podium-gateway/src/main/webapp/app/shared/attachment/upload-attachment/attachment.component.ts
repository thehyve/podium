/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, EventEmitter, Input, OnInit } from '@angular/core';
import { UploadOutput, UploadInput, UploadFile, humanizeBytes, UploaderOptions } from 'ngx-uploader';
import { AuthServerProvider } from '../../auth/auth-jwt.service';

@Component({
    selector: 'pdm-attachment',
    templateUrl: './attachment.component.html',
    styleUrls: ['attachment.scss']
})
export class AttachmentComponent implements OnInit {

    @Input() requestBaseId: string;

    public options: UploaderOptions;
    public files: UploadFile[];
    public uploadInput: EventEmitter<UploadInput>;
    public humanizeBytes: Function;
    public dragOver: boolean;

    constructor(
        private authServerProvider: AuthServerProvider,
    ) {
        this.files = []; // local uploading files array
        this.uploadInput = new EventEmitter<UploadInput>(); // input events, we use this to emit data to ngx-uploader
        this.humanizeBytes = humanizeBytes;
    }

    ngOnInit(): void {

    }

    onUploadOutput(output: UploadOutput): void {
        let token = this.authServerProvider.getToken();
        if (output.type === 'allAddedToQueue') { // when all files added in queue
            // uncomment this if you want to auto upload files when added
            const event: UploadInput = {
                type: 'uploadAll',
                url: '/api/requests/' + this.requestBaseId + '/addfile',
                headers: { 'Authorization': 'Bearer ' + token },
                method: 'POST'
            };
            this.uploadInput.emit(event);
        } else if (output.type === 'addedToQueue'  && typeof output.file !== 'undefined') { // add file to array when added
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
        }
    }

    startUpload(): void {
        const event: UploadInput = {
            type: 'uploadAll',
            url: '/api/requests/' + this.requestBaseId + '/addfile',
            method: 'POST',
            data: { foo: 'bar' }
        };

        this.uploadInput.emit(event);
    }

    cancelUpload(id: string): void {
        this.uploadInput.emit({ type: 'cancel', id: id });
    }

    removeFile(id: string): void {
        this.uploadInput.emit({ type: 'remove', id: id });
    }

    removeAllFiles(): void {
        this.uploadInput.emit({ type: 'removeAll' });
    }

}
