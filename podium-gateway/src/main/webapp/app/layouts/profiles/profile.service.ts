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
import { ProfileInfo } from './profile-info.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {

    private profileInfoUrl = 'api/profile-info';

    constructor(private http: HttpClient) { }

    getProfileInfo(): Observable<ProfileInfo> {
        return this.http.get<ProfileInfo>(this.profileInfoUrl)
            .map((data) => {
                let pi = new ProfileInfo();
                pi.activeProfiles = data.activeProfiles;
                pi.ribbonEnv = data.ribbonEnv;
                pi.inProduction = data.activeProfiles.indexOf('prod') !== -1;
                pi.swaggerEnabled = data.activeProfiles.indexOf('swagger') !== -1;
                return pi;
            });
    }
}
