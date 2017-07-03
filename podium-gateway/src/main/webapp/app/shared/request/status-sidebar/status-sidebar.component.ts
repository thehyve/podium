/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, Input } from '@angular/core';
import { RequestStatusSidebarOptions, StatusSidebarOptionsCollection } from './status-sidebar-options';
import { OverviewService } from '../../overview/overview.service';
import { UserGroupAuthority } from '../../authority/authority.constants';
import {
    RequestOverviewStatusOption
} from '../request-status/request-status.constants';
import { Response } from '@angular/http';
import { Subject, Subscription } from 'rxjs';

@Component({
    selector: 'pdm-request-status-sidebar',
    templateUrl: './status-sidebar.component.html',
    styleUrls: ['status-sidebar.scss']
})

export class RequestStatusSidebarComponent implements OnInit {

    public statusSidebarOptions = StatusSidebarOptionsCollection;

    public onStatusChange: Subject<RequestOverviewStatusOption> = new Subject();

    public overviewSubscription: Subscription;

    public activeStatus: RequestOverviewStatusOption;

    @Input()
    public userGroupAuthority: UserGroupAuthority;

    @Input()
    public pageParams: Function;

    @Input()
    public toggled: boolean;

    public counts = {};

    constructor(
        private overviewService: OverviewService
    ) {
    }

    ngOnInit() {
        this.activeStatus = this.overviewService.activeStatus;

        this.overviewSubscription = this.overviewService.onOverviewUpdate.subscribe(
            () => {
                this.activeStatus = this.overviewService.activeStatus;
            }
        );

        this.fetchCounts();
    }

    updateOverviewForStatus(overviewStatus: RequestOverviewStatusOption) {
        this.onStatusChange.next(overviewStatus);
    }

    isActiveElement(status: RequestOverviewStatusOption): boolean {
        return this.activeStatus === status;
    }

    fetchCounts() {
        this.overviewService.getRequestCountsForUserGroupAuthority(this.userGroupAuthority)
            .subscribe((res: Response) => {
                this.counts = res.json();
            });
    }

    /**
     * Determine if the a StatusSidebarOption should be included in the view based on the current UserGroupAuthority
     *
     * @param requiredUserGroups All UserGroupAuthority options in which the status should be included.
     * @returns {boolean} true if the view is for the correct current UserGroupAuthority
     */
    includeAsOption(requiredUserGroups: UserGroupAuthority[]) {
        return requiredUserGroups.indexOf(this.userGroupAuthority) > -1;
    }

    isFirstInGroup(overviewStatus: typeof RequestStatusSidebarOptions) {
        return overviewStatus.groupOrder === 1;
    }
}
