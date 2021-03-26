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
import { Http, Response, URLSearchParams, BaseRequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Organisation } from './organisation.model';
import { HttpHelper } from '../util/http-helper';

@Injectable()
export class OrganisationService {

    private resourceUrl = 'podiumuaa/api/organisations';
    private resourceSearchUrl = 'podiumuaa/api/_search/organisations';

    constructor(private http: Http) { }

    create(organisation: Organisation): Observable<Response> {
        let copy: Organisation = Object.assign({}, organisation);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    update(organisation: Organisation): Observable<Response> {
        let copy: Organisation = Object.assign({}, organisation);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    find(id: number): Observable<Organisation> {
        return this.http.get(`${this.resourceUrl}/${id}`).map((res: Response) => {
            return res.json();
        });
    }

    findAllAvailable(): Observable<Organisation> {
        return this.http.get(`${this.resourceUrl}/available`).map((res: Response) => {
            return res.json();
        });
    }

    findByUuid(uuid: string): Observable<Organisation> {
        return this.http.get(`${this.resourceUrl}/uuid/${uuid}`).map((res: Response) => {
            return res.json();
        });
    }

    query(req?: any): Observable<Response> {
        let options = HttpHelper.createRequestOption(req);
        return this.http.get(`${this.resourceUrl}/admin`, options);
    }

    activate(uuid: string, activate: boolean): Observable<Response> {
        return this.http.put(`${this.resourceUrl}/${uuid}/activation?value=${activate}`, {}).map((res: Response) => {
            return res.json();
        });
    }

    delete(uuid: string): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${uuid}`);
    }

    search(req?: any): Observable<Response> {
        let options = HttpHelper.createRequestOption(req);
        return this.http.get(this.resourceSearchUrl, options);
    }

    findAll(): Observable<Response> {
        return this.http.get(`${this.resourceUrl}`).map((res: Response) => {
            return res.json();
        });
    }

    jsonArrayToOrganisations(arr: any) {
        return arr.map((item) => {
            return new Organisation (item);
        });
    }

    convertUuidsToOrganisations(uuids: any[], allOrganisations: Organisation[]) {
        let temp = [];
        for (let selectedOrganisation of uuids) {
            let found = allOrganisations.find((organisation) => {
                return selectedOrganisation.uuid === organisation.uuid;
            });
            if (found) {
                temp.push(found);
            }
        }
        return temp;
    }

}
