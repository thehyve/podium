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
import { map, shareReplay } from 'rxjs/operators';

import { ProfileInfo } from './profile-info.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {

    private profileInfoUrl = 'api/profile-info';
    private profileInfo$?: Observable<ProfileInfo>;

    constructor(private http: HttpClient) { }

    getProfileInfo(): Observable<ProfileInfo> {
        if (this.profileInfo$) {
            return this.profileInfo$;
        }

        this.profileInfo$ = this.http.get<ProfileInfo>(this.profileInfoUrl).pipe(
            map((response) => {
                let profileInfo: ProfileInfo = {
                    activeProfiles: response.activeProfiles,
                    ribbonEnv: response.ribbonEnv,
                    inProduction: response.activeProfiles?.includes('prod'),
                    swaggerEnabled: response.activeProfiles?.includes('api-docs'),
                };
                return profileInfo;
            }),
            shareReplay()
        );
        return this.profileInfo$;
    }
}
