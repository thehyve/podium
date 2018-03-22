/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Http, Response, ResponseContentType } from '@angular/http';
import { Attachment } from './attachment.model';
import { User } from '../user/user.model';
import { AttachmentTypes } from './attachment.constants';

@Injectable()
export class AttachmentsService {

    private resourceUrl = 'api/requests';

    constructor(private http: Http) {

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
     * @param {string} requestUuid - request uuid
     * @param {string} fileUuid - file uuid
     * @returns {Observable<Attachment[]>}
     */
    downloadAttachment(requestUuid: string, fileUuid: string) {
        return this.http.get(`${this.resourceUrl}/${requestUuid}/files/${fileUuid}/download`, {
            responseType: ResponseContentType.Blob
        }).map((response: Response) => {
            return <Attachment[]> response.json();
        });
    }

    /**
     * Get all attachments for a request
     * @param {string} requestUuid - request uuid
     * @returns {Observable<Response>}
     */
    getAttachments(requestUuid: string): Observable<Attachment[]> {
        return this.http.get(`${this.resourceUrl}/${requestUuid}/files`).map((response: Response) => {
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
