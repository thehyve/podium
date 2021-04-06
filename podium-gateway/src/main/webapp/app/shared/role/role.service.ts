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
import { Role } from './role.model';

@Injectable()
export class RoleService {

    private resourceUrl = 'podiumuaa/api/roles';

    constructor(private http: Http) { }

    update(role: Role): Observable<Role> {
        let copy: Role = Object.assign({}, role);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            return res.json();
        });
    }

    find(id: number): Observable<Role> {
        return this.http.get(`${this.resourceUrl}/${id}`).map((res: Response) => {
            return res.json();
        });
    }

    findAllRolesForOrganisation(uuid: string): Observable<Role[]> {
        return this.http.get(`${this.resourceUrl}/organisation/${uuid}`).map((res: Response) => {
            return res.json();
        });
    }
}
