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
import { Observable } from 'rxjs/Rx';
import { User } from './user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
    private resourceUrl = 'podiumuaa/api/users';
    private resourceSuggestUrl = 'podiumuaa/api/_suggest/users';

    constructor(private http: HttpClient) { }

    create(user: User): Observable<any> {
        return this.http.post(this.resourceUrl, user);
    }

    update(user: User): Observable<any> {
        return this.http.put(this.resourceUrl, user);
    }

    unlock(user: User): Observable<any> {
        return this.http.put(`${this.resourceUrl}/uuid/${user.uuid}/unlock`, {});
    }

    find(login: string): Observable<User> {
        return this.http.get<User>(`${this.resourceUrl}/${login}`);
    }

    findByUuid(uuid: string): Observable<User> {
        return this.http.get<User>(`${this.resourceUrl}/uuid/${uuid}`);
    }

    suggest(req?: any): Observable<any> {
        return this.http.get(`${this.resourceSuggestUrl}`, {
            params: req
        });
    }

    delete(login: string): Observable<any> {
        return this.http.delete(`${this.resourceUrl}/${login}`);
    }
}
