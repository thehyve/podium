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
import { Log } from './log.model';

@Injectable({ providedIn: 'root' })
export class LogsService {
    constructor(private http: HttpClient) { }

    changeLevel(log: Log): Observable<any> {
        return this.http.put('management/logs', log);
    }

    findAll(): Observable<Log[]> {
        return this.http.get<Log[]>('management/logs');
    }
}
