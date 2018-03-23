/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { EventEmitter, Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Http, Response, ResponseContentType } from '@angular/http';
import { Attachment } from './attachment.model';
import { AttachmentTypes } from './attachment.constants';
import { UploadInput } from 'ngx-uploader/index';
import { AuthServerProvider } from '../auth/auth-jwt.service';
import { RequestBase } from '../request';

@Injectable()
export class AttachmentService {

    private resourceUrl = 'api/requests';
    // input events, we use this to emit data to ngx-uploader
    private uploadInput = new EventEmitter<UploadInput>();

    constructor(private http: Http, private authServerProvider: AuthServerProvider) {
    }

    /**
     * Upload file
     */
    uploadRequestFile(request: RequestBase) {
        let token = this.authServerProvider.getToken();
        const event: UploadInput = {
            type: 'uploadAll',
            url: `${this.resourceUrl}/${request.uuid}/files`,
            headers: {'Authorization': 'Bearer ' + token},
            method: 'POST'
        };
        this.uploadInput.emit(event);
    }

    /**
     * Remove an attachment
     * @returns {Observable<Response>}
     */
    remove(attachment: Attachment): Observable<Response> {
        return this.http.delete(
            `${this.resourceUrl}/${attachment.request.uuid}/files/${attachment.uuid}`);
    }

    /**
     * Download an attachment
     * @param {string} request - request
     * @param {string} fileUuid - file uuid
     * @returns {Observable<Attachment[]>}
     */
    downloadAttachment(request: RequestBase, fileUuid: string) {
        return this.http.get(`${this.resourceUrl}/${request.uuid}/files/${fileUuid}/download`, {
            responseType: ResponseContentType.Blob
        }).map((response: Response) => {
            return <Attachment[]> response.json();
        });
    }

    /**
     * Get all attachments for a request
     * @param {string} request - request uuid
     * @returns {Observable<Response>}
     */
    getAttachments(request: RequestBase): Observable<Attachment[]> {
        return this.http.get(`${this.resourceUrl}/${request.uuid}/files`).map((response: Response) => {
            return <Attachment[]> response.json();
        });
    }

    /**
     * Set attachment type
     * @param {Attachment} attachment
     * @returns {Observable<Response>}
     */
    setAttachmentType(attachment: Attachment): Observable<Response> {
        return this.http.put(
            `${this.resourceUrl}/${attachment.request.uuid}/files/${attachment.uuid}/type`, attachment)
            .map((response: Response) => { return response.json(); });
    }

    /**
     * Tests if at least one attachment does not have attachment type
     * @param {Attachment[]} attachments
     * @returns {boolean}
     */
    hasAttachmentsTypeNone(attachments: Attachment[]): boolean {
        return attachments.some( attachment => {
           return attachment.requestFileType === AttachmentTypes.NONE;
        });
    }

}
