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
import { ApplicationConfigService } from '../../../core/config/application-config.service';

@Injectable({ providedIn: 'root' })
export class PasswordResetInit {

    constructor (
        private config: ApplicationConfigService,
        private http: HttpClient,
    ) {}

    save(mail: string): Observable<any> {
        let url = this.config.getUaaEndpoint('api/account/reset_password/init');
        return this.http.post(url, mail, { responseType: 'text' });
    }
}
