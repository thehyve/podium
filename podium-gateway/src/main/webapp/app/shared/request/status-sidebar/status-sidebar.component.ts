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
import { JhiLanguageService } from 'ng-jhipster';
import { RequestStatusSidebarOptions, StatusSidebarOptionsCollection, StatusSidebarOption } from './status-sidebar-options';
import { OverviewService } from '../../overview/overview.service';
import { UserGroupAuthority } from '../../authority/authority.constants';
import { StatusType, RequestStatusOptions } from '../request-status/request-status.constants';
import { Response } from '@angular/http';
import { Subject, Subscription } from 'rxjs';

@Component({
    selector: 'pdm-request-status-sidebar',
    templateUrl: './status-sidebar.component.html',
    styleUrls: ['status-sidebar.scss']
})

export class RequestStatusSidebarComponent implements OnInit {

    public statusSidebarOptions = StatusSidebarOptionsCollection;

    public onStatusChange: Subject<StatusSidebarOption> = new Subject();

    public overviewSubscription: Subscription;

    public activeStatus: StatusSidebarOption;

    @Input()
    public userGroupAuthority: UserGroupAuthority;

    @Input()
    public pageParams: Function;

    @Input()
    public toggled: boolean;

    public counts = {};

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private overviewService: OverviewService
    ) {
        this.jhiLanguageService.addLocation('requestSidebar');
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

    updateOverviewForStatus(overviewStatus: StatusSidebarOption) {
        this.onStatusChange.next(overviewStatus);
    }

    isActiveElement(status: StatusSidebarOption): boolean {
        return this.activeStatus === status;
    }

    fetchCounts() {
        this.overviewService.getRequestCountsForUserGroupAuthority(this.userGroupAuthority)
            .subscribe((res: Response) => {
                this.counts = res.json();
            });
    }

    includeAsOption(requiredUserGroups: UserGroupAuthority[]) {
        return requiredUserGroups.indexOf(this.userGroupAuthority) > -1;
    }
}
