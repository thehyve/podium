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
import { Audit } from './audit.model';

@Injectable({ providedIn: 'root' })
export class AuditsService  {
    constructor (
        private config: ApplicationConfigService,
        private http: HttpClient,
    ) {}

    query(req: any): Observable<HttpResponse<Audit[]>> {
        let url = this.config.getUaaEndpoint('management/audits');
        return this.http.get<Audit[]>(url, {
            params: req,
            observe: 'response'
        });
    }
}
