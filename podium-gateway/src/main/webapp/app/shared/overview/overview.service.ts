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
import { Http, Response } from '@angular/http';
import { UserGroupAuthority } from '../authority/authority.constants';
import { Observable, Subject } from 'rxjs';
import { OverviewServiceConfig } from './overview.service.config';
import { HttpHelper } from '../util/http-helper';
import { RequestOverviewStatusOption } from '../request/request-status/request-status.constants';

@Injectable()
export class OverviewService {

    resourceUrl: string;
    resourceSearchUrl: string;

    public activeStatus: RequestOverviewStatusOption;
    public onOverviewUpdate: Subject<Response> = new Subject();

    constructor(
        private http: Http,
        @Optional() config: OverviewServiceConfig
    ) {
        if (config) {
            this.resourceUrl = config.resourceUrl;
            this.resourceSearchUrl = config.resourceSearchUrl;
        } else {
            console.error('No overview config set!');
        }
    }

    findRequestsForOverview(
        requestOptions: any,
        requestStatus: RequestOverviewStatusOption,
        userGroup: UserGroupAuthority
    ): Observable<Response> {
        let options = HttpHelper.createRequestOption(requestOptions);
        let requestsUrl;

        if (!requestStatus || !userGroup) {
            return;
        }

        this.activeStatus = requestStatus;

        // When we have to filter for Drafts
        if (requestStatus === RequestOverviewStatusOption.Draft) {
            return this.http.get(`${this.resourceUrl}/drafts`, options).map((res: Response) => {
                return res;
            });
        }

        switch (userGroup) {
            case UserGroupAuthority.Requester:
                requestsUrl = `${this.resourceUrl}/status/${requestStatus}/${UserGroupAuthority.Requester}/`;
                break;
            case UserGroupAuthority.Coordinator:
                requestsUrl = `${this.resourceUrl}/status/${requestStatus}/${UserGroupAuthority.Coordinator}`;
                break;
            case UserGroupAuthority.Reviewer:
                requestsUrl = `${this.resourceUrl}/reviewer`;
                break;
            default:
                return;
        }

        return this.http.get(requestsUrl, options).map((res: Response) => {
            return res;
        });
    }

    /**
     * Fetch organisations using pagination and search parameters
     *
     * @param requestOptions search parameters
     * @returns {Observable<Response>} the response
     */
    findOrganisationsForOverview(
        requestOptions: any
    ): Observable<Response> {
        let options = HttpHelper.createRequestOption(requestOptions);
        return this.http.get(`${this.resourceUrl}/admin`, options);
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
    ): Observable<Response> {
        let options = HttpHelper.createRequestOption(requestOptions);
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
        return this.http.get(`${usersUrl}`, options);
    }

    getRequestCountsForUserGroupAuthority(userGroupAuthority: UserGroupAuthority): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/counts/${userGroupAuthority}`).map((res: Response) => {
            return res;
        });
    }

    public overviewUpdateEvent(response: Response) {
        this.onOverviewUpdate.next(response);
    }

}
