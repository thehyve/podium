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
import { parseLinks } from '../../../shared/util/parse-links-util';
import { EventManager } from '../../../core/util/event-manager.service';
import { AccountService } from '../../../core/auth/account.service';
import { Account } from '../../../core/auth/account.model';
import { UserService } from '../../../shared/user/user.service';
import { User } from '../../../shared/user/user.model';
import { Overview } from '../../../shared/overview/overview';
import { OverviewService } from '../../../shared/overview/overview.service';
import { Subscription } from 'rxjs';
import { UserGroupAuthority } from '../../../shared/authority/authority.constants';
import { ApplicationConfigService } from '../../../core/config/application-config.service';

@Component({
    selector: 'pdm-user-mgmt',
    templateUrl: './user-management.component.html',
    providers: [
        {
            provide: OverviewService,
            useFactory: (
                config: ApplicationConfigService,
                http: HttpClient,
            ) => {
                let serviceConfig = {
                    getUaaEndpoint(path: string) {
                        return config.getUaaEndpoint(`api/users/${path}`);
                    },
                } as ApplicationConfigService;
                return new OverviewService(serviceConfig, http);
            },
            deps: [
                HttpClient
            ]
        }
    ]
})
export class UserMgmtComponent extends Overview implements OnInit, OnDestroy {

    currentAccount: Account;
    users: User[];
    error: any;
    success: any;
    overviewSubscription: Subscription;
    eventSubscription: Subscription;
    userGroupAuthority: UserGroupAuthority;

    constructor(
        private userService: UserService,
        private accountService: AccountService,
        private eventManager: EventManager,
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
        this.accountService.identity().then((account) => {
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
        this.links = parseLinks(headers.get('link'));
        this.totalItems = headers.get('x-total-count');
        this.queryCount = this.totalItems;
        this.users = users;
    }

    trackIdentity (index, item: User) {
        return item.id;
    }
}
