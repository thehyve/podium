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
import { Http, BaseRequestOptions, URLSearchParams, Response } from '@angular/http';
import {
    RequestStatusOptions, RequestReviewStatusOptions,
    StatusType
} from '../request/request-status/request-status.constants';
import { UserGroupAuthority } from '../authority/authority.constants';
import { Observable, Subject } from 'rxjs';
import { OverviewServiceConfig } from './overview.service.config';
import { StatusSidebarOption } from '../request/status-sidebar/status-sidebar-options';

@Injectable()
export class OverviewService {

    resourceUrl: string;
    resourceSearchUrl: string;

    public activeStatus: StatusSidebarOption;
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
        requestStatus: StatusSidebarOption,
        userGroup: UserGroupAuthority
    ): Observable<Response> {
        let options = this.createRequestOption(requestOptions);
        let requestsUrl;

        if (!requestStatus || !userGroup) {
            return;
        }

        this.activeStatus = requestStatus;

        // When we have to filter for Drafts
        if (requestStatus === StatusSidebarOption.Draft && UserGroupAuthority.Requester) {
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

    getRequestCountsForUserGroupAuthority(userGroupAuthority: UserGroupAuthority): Observable<Response> {
        return this.http.get(`${this.resourceUrl}/counts/${userGroupAuthority}`).map((res: Response) => {
            return res;
        });
    }

    private createRequestOption(req?: any): BaseRequestOptions {
        let options: BaseRequestOptions = new BaseRequestOptions();
        if (req) {
            let params: URLSearchParams = new URLSearchParams();
            params.set('page', req.page);
            params.set('size', req.size);
            if (req.sort) {
                params.paramsMap.set('sort', req.sort);
            }
            params.set('query', req.query);

            options.params = params;
        }
        return options;
    }

    public overviewUpdateEvent(response: Response) {
        this.onOverviewUpdate.next(response);
    }

}
