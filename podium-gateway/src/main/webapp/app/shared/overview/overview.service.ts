/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Injectable, Optional } from '@angular/core';
import { HttpClient, HttpResponse, HttpResponseBase } from '@angular/common/http';
import { UserGroupAuthority } from '../authority/authority.constants';
import { Observable, Subject } from 'rxjs';
import { OverviewServiceConfig } from './overview.service.config';
import { RequestOverviewStatusOption } from '../request/request-status/request-status.constants';
import { RequestBase } from '../request/request-base';

@Injectable()
export class OverviewService {

    resourceUrl: string;

    public activeStatus: RequestOverviewStatusOption;
    public onOverviewUpdate: Subject<HttpResponseBase> = new Subject();

    constructor(
        private http: HttpClient,
        @Optional() config: OverviewServiceConfig
    ) {
        if (config) {
            this.resourceUrl = config.resourceUrl;
        } else {
            console.error('No overview config set!');
        }
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
        let baseUrl = this.resourceUrl;

        // When we have to filter for Drafts
        if (requestStatus === RequestOverviewStatusOption.Draft) {
            return this.http.get<RequestBase[]>(`${baseUrl}/drafts`, {
                params: requestOptions,
                observe: 'response'
            });
        }

        let UGA = UserGroupAuthority;
        let urlByGroup = {
            [UGA.Requester]: `${baseUrl}/status/${requestStatus}/${UGA.Requester}/`,
            [UGA.Coordinator]: `${baseUrl}/status/${requestStatus}/${UGA.Coordinator}`,
            [UGA.Reviewer]: `${baseUrl}/reviewer`,
        };
        let requestUrl = urlByGroup[userGroup];
        if (!requestUrl) {
            return;
        }

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
        return this.http.get(`${this.resourceUrl}/admin`, {
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
                usersUrl = `${this.resourceUrl}/organisations`;
                break;
            case UserGroupAuthority.BbmriAdmin:
            case UserGroupAuthority.PodiumAdmin:
                usersUrl = this.resourceUrl;
                break;
            default:
                console.error('No user group authority set for this overview!');
                return Observable.throw('No user group authority set for this overview!');
        }
        return this.http.get(`${usersUrl}`, {
            params: requestOptions,
            observe: 'response'
        });
    }

    getRequestCountsForUserGroupAuthority(userGroupAuthority: UserGroupAuthority) {
        type R = { [status: string]: number };
        return this.http.get<R>(`${this.resourceUrl}/counts/${userGroupAuthority}`);
    }

    public overviewUpdateEvent(response: HttpResponseBase) {
        this.onOverviewUpdate.next(response);
    }

}
