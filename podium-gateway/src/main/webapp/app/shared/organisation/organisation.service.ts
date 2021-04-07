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
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Organisation } from './organisation.model';

@Injectable()
export class OrganisationService {

    private resourceUrl = 'podiumuaa/api/organisations';

    constructor(private http: HttpClient) { }

    create(organisation: Organisation): Observable<Organisation> {
        let copy: Organisation = Object.assign({}, organisation);
        return this.http.post<Organisation>(this.resourceUrl, copy);
    }

    update(organisation: Organisation): Observable<Organisation> {
        let copy: Organisation = Object.assign({}, organisation);
        return this.http.put<Organisation>(this.resourceUrl, copy);
    }

    findAllAvailable(): Observable<Organisation> {
        return this.http.get<Organisation>(`${this.resourceUrl}/available`);
    }

    findByUuid(uuid: string): Observable<Organisation> {
        return this.http.get<Organisation>(`${this.resourceUrl}/uuid/${uuid}`);
    }

    activate(uuid: string, activate: boolean): Observable<HttpResponse<any>> {
        return this.http.put(`${this.resourceUrl}/${uuid}/activation?value=${activate}`, {}, {
            observe: 'response'
        });
    }

    delete(uuid: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${uuid}`);
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
