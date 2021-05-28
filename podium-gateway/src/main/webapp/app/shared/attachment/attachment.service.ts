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
import { Observable } from 'rxjs';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Attachment } from './attachment.model';
import { AttachmentTypes } from './attachment.constants';
import { UploadInput } from 'ngx-uploader';
import { AuthServerProvider } from '../../core/auth/auth-jwt.service';
import { RequestBase } from '../request/request-base';

@Injectable({ providedIn: 'root' })
export class AttachmentService {

    private resourceUrl = 'api/requests';

    constructor(private http: HttpClient, private authServerProvider: AuthServerProvider) {
    }

    /**
     * Upload file
     */
    uploadRequestFile(request: RequestBase, uploadInputEventEmitter: EventEmitter<UploadInput>) {
        let token = this.authServerProvider.getToken();
        const event: UploadInput = {
            type: 'uploadAll',
            url: `${this.resourceUrl}/${request.uuid}/files`,
            headers: {'Authorization': 'Bearer ' + token},
            method: 'POST'
        };
        uploadInputEventEmitter.emit(event);
    }

    /**
     * Remove an attachment
     */
    remove(attachment: Attachment): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/${attachment.request.uuid}/files/${attachment.uuid}`, {
            observe: 'response'
        });
    }

    /**
     * Download an attachment
     * @param {string} request - request
     * @param {string} fileUuid - file uuid
     */
    downloadAttachment(request: RequestBase, fileUuid: string) {
        return this.http.get(`${this.resourceUrl}/${request.uuid}/files/${fileUuid}/download`, {
            responseType: 'blob'
        });
    }

    /**
     * Get all attachments for a request
     * @param {string} request - request uuid
     */
    getAttachments(request: RequestBase): Observable<Attachment[]> {
        return this.http.get<Attachment[]>(`${this.resourceUrl}/${request.uuid}/files`);
    }

    /**
     * Set attachment type
     */
    setAttachmentType(attachment: Attachment): Observable<any> {
        let url = `${this.resourceUrl}/${attachment.request.uuid}/files/${attachment.uuid}/type`;
        return this.http.put(url, attachment);
    }

    /**
     * Tests if at least one attachment does not have attachment type
     */
    hasAttachmentsTypeNone(attachments: Attachment[]): boolean {
        return attachments.some( attachment => {
           return attachment.requestFileType === AttachmentTypes.NONE;
        });
    }

}
