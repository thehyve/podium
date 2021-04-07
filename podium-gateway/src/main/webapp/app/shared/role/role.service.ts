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
import { Role } from './role.model';

@Injectable({ providedIn: 'root' })
export class RoleService {

    private resourceUrl = 'podiumuaa/api/roles';

    constructor(private http: HttpClient) { }

    update(role: Role): Observable<Role> {
        let copy: Role = Object.assign({}, role);
        return this.http.put<Role>(this.resourceUrl, copy);
    }

    find(id: number): Observable<Role> {
        return this.http.get<Role>(`${this.resourceUrl}/${id}`);
    }

    findAllRolesForOrganisation(uuid: string): Observable<Role[]> {
        return this.http.get<Role[]>(`${this.resourceUrl}/organisation/${uuid}`);
    }
}
