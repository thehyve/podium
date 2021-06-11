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
import { ApplicationConfigService } from '../../core/config/application-config.service';
import { Organisation } from './organisation.model';

@Injectable({ providedIn: 'root' })
export class OrganisationService {

    constructor (
        private config: ApplicationConfigService,
        private http: HttpClient,
    ) {}

    private getUrl(path: string) {
        return this.config.getUaaEndpoint(`api/organisations/${path}`);
    }

    create(organisation: Organisation): Observable<Organisation> {
        let copy: Organisation = Object.assign({}, organisation);
        let url = this.getUrl('')
        return this.http.post<Organisation>(url, copy);
    }

    update(organisation: Organisation): Observable<Organisation> {
        let copy: Organisation = Object.assign({}, organisation);
        let url = this.getUrl('');
        return this.http.put<Organisation>(url, copy);
    }

    findAllAvailable(): Observable<Organisation> {
        let url = this.getUrl('available');
        return this.http.get<Organisation>(url);
    }

    findByUuid(uuid: string): Observable<Organisation> {
        let url = this.getUrl(`uuid/${uuid}`);
        return this.http.get<Organisation>(url);
    }

    activate(uuid: string, activate: boolean): Observable<HttpResponse<any>> {
        let url = this.getUrl(`${uuid}/activation?value=${activate}`)
        return this.http.put(url, {}, {
            observe: 'response'
        });
    }

    delete(uuid: string): Observable<any> {
        let url = this.getUrl(uuid);
        return this.http.delete(url);
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
