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
import { User } from './user.model';
import { ApplicationConfigService } from '../../core/config/application-config.service';

@Injectable({ providedIn: 'root' })
export class UserService {
    constructor (
        private config: ApplicationConfigService,
        private http: HttpClient,
    ) {}

    private getUrl(path: string) {
        return this.config.getUaaEndpoint(`api/users/${path}`);
    }

    private getSuggestUrl(path: string) {
        return this.config.getUaaEndpoint(`api/_suggest/users/${path}`);
    }

    create(user: User): Observable<any> {
        let url = this.getUrl('');
        return this.http.post(url, user);
    }

    update(user: User): Observable<any> {
        let url = this.getUrl('');
        return this.http.put(url, user);
    }

    unlock(user: User): Observable<any> {
        let url = this.getUrl(`uuid/${user.uuid}/unlock`);
        return this.http.put(url, {});
    }

    find(login: string): Observable<User> {
        let url = this.getUrl(login);
        return this.http.get<User>(url);
    }

    findByUuid(uuid: string): Observable<User> {
        let url = this.getUrl(`uuid/${uuid}`);
        return this.http.get<User>(url);
    }

    suggest(req?: any): Observable<any> {
        let url = this.getSuggestUrl('');
        return this.http.get(url, {
            params: req
        });
    }

    delete(login: string): Observable<any> {
        let url = this.getUrl(login);
        return this.http.delete(url);
    }
}
