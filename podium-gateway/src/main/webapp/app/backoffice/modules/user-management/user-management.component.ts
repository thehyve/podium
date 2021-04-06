/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { JhiEventManager, JhiParseLinks, JhiAlertService } from 'ng-jhipster';
import { Principal } from '../../../shared/auth/principal.service';
import { UserService } from '../../../shared/user/user.service';
import { User } from '../../../shared/user/user.model';
import { Overview } from '../../../shared/overview/overview';
import { OverviewService } from '../../../shared/overview/overview.service';
import { OverviewServiceConfig } from '../../../shared/overview/overview.service.config';
import { Subscription } from 'rxjs';
import { UserGroupAuthority } from '../../../shared/authority/authority.constants';

let overviewConfig: OverviewServiceConfig = {
    resourceUrl: 'podiumuaa/api/users',
    resourceSearchUrl: 'podiumuaa/api/_search/users'
};

@Component({
    selector: 'pdm-user-mgmt',
    templateUrl: './user-management.component.html',
    providers: [
        {
            provide: OverviewService,
            useFactory: (http: HttpClient) => {
                return new OverviewService(http, overviewConfig);
            },
            deps: [
                HttpClient
            ]
        }
    ]
})
export class UserMgmtComponent extends Overview implements OnInit, OnDestroy {

    currentAccount: any;
    users: User[];
    error: any;
    success: any;
    overviewSubscription: Subscription;
    eventSubscription: Subscription;
    userGroupAuthority: UserGroupAuthority;

    constructor(
        private userService: UserService,
        private parseLinks: JhiParseLinks,
        private alertService: JhiAlertService,
        private principal: Principal,
        private eventManager: JhiEventManager,
        private overviewService: OverviewService,
        protected activatedRoute: ActivatedRoute,
        protected router: Router
    ) {
        super(router, activatedRoute);

        this.overviewSubscription = this.overviewService.onOverviewUpdate.subscribe(
            (res: HttpResponse<User[]>) =>
                this.processAvailableUsers(res.body, res.headers)
        );
    }

    ngOnInit() {
        this.userGroupAuthority = this.activatedRoute.snapshot.data['userAuthorityGroup'];
        this.principal.identity().then((account) => {
            this.currentAccount = account;
            this.fetchUsers();
            this.registerChangeInUsers();
        });
    }

    ngOnDestroy() {
        if (this.eventSubscription) {
            this.eventManager.destroy(this.eventSubscription);
        }

        if (this.overviewSubscription) {
            this.overviewSubscription.unsubscribe();
        }
    }

    registerChangeInUsers() {
        this.eventSubscription = this.eventManager.subscribe('userListModification', () => {
            this.fetchUsers();
        });
    }

    unlock (user) {
        this.userService.unlock(user).subscribe(
            response => {
                if (response.status === 200) {
                    this.error = null;
                    this.success = 'OK';
                    this.fetchUsers();
                } else {
                    this.success = null;
                    this.error = 'ERROR';
                }
            });
    }

    transitionUsers() {
        this.transition();
        this.fetchUsers();
    }

    fetchUsers() {
        this.overviewService.findUsersForOverview(this.userGroupAuthority, this.getPageParams())
            .subscribe((res) => this.overviewService.overviewUpdateEvent(res));
    }

    processAvailableUsers (users: User[], headers) {
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = headers.get('x-total-count');
        this.queryCount = this.totalItems;
        this.users = users;
    }

    trackIdentity (index, item: User) {
        return item.id;
    }
}
