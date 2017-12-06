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

@Injectable()
export class AttachmentsService {

    private resourceUrl = 'api/requests';

    constructor(private http: Http) {

    }

    /**
     * Attach a file
     * @returns {Observable<Response>}
     */
    attach(): Observable<Response> {
        console.log('in attach');
        return null;
    }

    /**
     * Remove an attachment
     * @returns {Observable<Response>}
     */
    remove(attachment: Attachment): Observable<Response> {
        return this.http.delete(
            `${this.resourceUrl}/deletefile/${attachment.uuid}`);
    }

    /**
     * Remove all attachment
     * @returns {Observable<Response>}
     */
    removeAll(): Observable<Response> {
        console.log('remove all attachment');
        return null;
    }


    /**
     * Format bytes
     * @param bytes
     * @param precision
     * @returns {string}
     */
    formatByte(bytes, precision): string {
        if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
        if (typeof precision === 'undefined') precision = 1;
        const units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'],
            number = Math.floor(Math.log(bytes) / Math.log(1024));
        return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) +  ' ' + units[number];
    }


    /**
     * Download an attachment
     * @param {string} uuid - request uuid
     * @param {string} fileuuid - file uuid
     * @returns {Observable<Attachment[]>}
     */
    downloadAttachment(uuid: string, fileuuid: string) {
        return this.http.get(`${this.resourceUrl}/${uuid}/file/${fileuuid}`, {
            responseType: ResponseContentType.Blob
        }).map((response: Response) => {
            return <Attachment[]> response.json();
        });
    }

    /**
     * Get all attachments
     * @returns {Observable<Response>}
     */
    getAttachments(uuid: string): Observable<Attachment[]> {
        return this.http.get(`${this.resourceUrl}/${uuid}/files`).map((response: Response) => {
            return <Attachment[]> response.json();
        });
    }

    /**
     * Set attachment type
     * @param {Attachment} attachment
     * @returns {Observable<Response>}
     */
    setAttachmentType(attachment: Attachment): Observable<Response> {
        return this.http.post(`${this.resourceUrl}/setfiletype/${attachment.uuid}`, attachment)
            .map((response: Response) => { return response.json();});
    }
}
