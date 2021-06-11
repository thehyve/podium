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
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from '../../core/config/application-config.service';
import { Role } from './role.model';

@Injectable({ providedIn: 'root' })
export class RoleService {

    constructor (
        private config: ApplicationConfigService,
        private http: HttpClient,
    ) {}

    private getUrl(path: string) {
        return this.config.getUaaEndpoint(`api/roles/${path}`);
    }

    update(role: Role): Observable<Role> {
        let copy: Role = Object.assign({}, role);
        let url = this.getUrl('');
        return this.http.put<Role>(url, copy);
    }

    find(id: number): Observable<Role> {
        let url = this.getUrl(String(id));
        return this.http.get<Role>(url);
    }

    findAllRolesForOrganisation(uuid: string): Observable<Role[]> {
        let url = this.getUrl(`organisation/${uuid}`);
        return this.http.get<Role[]>(url);
    }
}
