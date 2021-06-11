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
import { HttpClient, HttpResponse, HttpResponseBase } from '@angular/common/http';
import { UserGroupAuthority } from '../authority/authority.constants';
import { Observable, Subject, throwError } from 'rxjs';
import { RequestOverviewStatusOption } from '../request/request-status/request-status.constants';
import { RequestBase } from '../request/request-base';

export interface OverviewServiceConfig {
    getEndpoint(path: string): string;
}

@Injectable()
export class OverviewService {

    public activeStatus: RequestOverviewStatusOption;
    public onOverviewUpdate: Subject<HttpResponseBase> = new Subject();

    constructor(
        private config: OverviewServiceConfig,
        private http: HttpClient,
    ) {}

    private getUrl(path: string) {
        return this.config.getEndpoint(path);
    }

    findRequestsForOverview(
        requestOptions: any,
        requestStatus: RequestOverviewStatusOption,
        userGroup: UserGroupAuthority
    ): Observable<HttpResponse<RequestBase[]>> {
        if (!requestStatus || !userGroup) {
            return;
        }

        this.activeStatus = requestStatus;

        // When we have to filter for Drafts
        if (requestStatus === RequestOverviewStatusOption.Draft) {
            let draftsUrl = this.getUrl('drafts')
            return this.http.get<RequestBase[]>(draftsUrl, {
                params: requestOptions,
                observe: 'response'
            });
        }

        let UGA = UserGroupAuthority;
        let pathByGroup = {
            [UGA.Requester]: `status/${requestStatus}/${UGA.Requester}/`,
            [UGA.Coordinator]: `status/${requestStatus}/${UGA.Coordinator}`,
            [UGA.Reviewer]: `reviewer`,
        };
        let requestPath = pathByGroup[userGroup];
        if (!requestPath) {
            return;
        }

        let requestUrl = this.getUrl(requestPath)
        return this.http.get<RequestBase[]>(requestUrl, {
            params: requestOptions,
            observe: 'response',
        });
    }

    /**
     * Fetch organisations using pagination and search parameters
     *
     * @param requestOptions search parameters
     */
    findOrganisationsForOverview(
        requestOptions: any
    ): Observable<HttpResponseBase> {
        let url = this.getUrl('admin');
        return this.http.get(url, {
            params: requestOptions,
            observe: 'response'
        });
    }

    /**
     * Fetch users using pagination and search parameters
     *
     * @param userGroupAuthority the user group authority to fetch users for
     * @param requestOptions search parameters
     * @returns {Observable<Response>} the response
     */
    findUsersForOverview(
        userGroupAuthority: UserGroupAuthority,
        requestOptions: any
    ): Observable<HttpResponseBase> {
        let usersUrl;
        switch (userGroupAuthority) {
            case UserGroupAuthority.OrganisationAdmin:
                usersUrl = this.getUrl('organisations');
                break;
            case UserGroupAuthority.BbmriAdmin:
            case UserGroupAuthority.PodiumAdmin:
                usersUrl = this.getUrl('');
                break;
            default:
                console.error('No user group authority set for this overview!');
                return throwError('No user group authority set for this overview!');
        }
        return this.http.get(usersUrl, {
            params: requestOptions,
            observe: 'response'
        });
    }

    getRequestCountsForUserGroupAuthority(userGroupAuthority: UserGroupAuthority) {
        type R = { [status: string]: number };
        let url = this.getUrl(`counts/${userGroupAuthority}`);
        return this.http.get<R>(url);
    }

    public overviewUpdateEvent(response: HttpResponseBase) {
        this.onOverviewUpdate.next(response);
    }

}
