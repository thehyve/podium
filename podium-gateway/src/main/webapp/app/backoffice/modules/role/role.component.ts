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
import { Response } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager, ParseLinks, PaginationUtil, JhiLanguageService, AlertService } from 'ng-jhipster';

import { Role } from './role.model';
import { RoleService } from './role.service';
import { Organisation } from '../organisation/organisation.model';
import { OrganisationService } from '../organisation/organisation.service';
import { User } from '../../../shared/user/user.model';
import { UserService } from '../../../shared/user/user.service';
import { ITEMS_PER_PAGE, Principal } from '../../../shared';
import { PaginationConfig } from '../../../blocks/config/uib-pagination.config';
import { Authority } from '../../../shared/authority/authority';
import { AUTHORITIES_MAP } from '../../../shared/authority/authority.constants';

@Component({
    selector: 'jhi-role',
    templateUrl: './role.component.html'
})
export class RoleComponent implements OnInit, OnDestroy {

    currentAccount: any;
    roles: Role[];
    organisations: { [uuid: string]: Organisation; };
    users: { [uuid: string]: User; };
    authoritiesMap: { [token: string]: Authority; };

    error: any;
    success: any;
    eventSubscriber: Subscription;
    currentSearch: string;
    routeData: any;
    links: any;
    totalItems: any;
    queryCount: any;
    itemsPerPage: any;
    page: any;
    predicate: any;
    previousPage: any;
    reverse: any;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private roleService: RoleService,
        private organisationService: OrganisationService,
        private userService: UserService,
        private parseLinks: ParseLinks,
        private alertService: AlertService,
        private principal: Principal,
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private eventManager: EventManager,
        private paginationUtil: PaginationUtil,
        private paginationConfig: PaginationConfig
    ) {
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.routeData = this.activatedRoute.data.subscribe(data => {
            this.page = data['pagingParams'].page;
            this.previousPage = data['pagingParams'].page;
            this.reverse = data['pagingParams'].ascending;
            this.predicate = data['pagingParams'].predicate;
        });
        this.currentSearch = activatedRoute.snapshot.params['search'] ? activatedRoute.snapshot.params['search'] : '';
        this.jhiLanguageService.setLocations(['role']);
        this.authoritiesMap = AUTHORITIES_MAP;
        this.organisations = {};
        this.users = {};
    }

    loadAll() {
        if (this.currentSearch) {
            this.roleService.search({
                query: this.currentSearch,
                size: this.itemsPerPage,
                sort: this.sort()
            }).subscribe(
                (res: Response) => this.onSuccess(res.json(), res.headers),
                (res: Response) => this.onError(res.json())
            );
            return;
        }
        this.roleService.query({
            page: this.page - 1,
            size: this.itemsPerPage,
            sort: this.sort()
        }).subscribe(
            (res: Response) => this.onSuccess(res.json(), res.headers),
            (res: Response) => this.onError(res.json())
        );
    }
    loadPage (page: number) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }
    transition() {
        this.router.navigate(['/role'], {queryParams:
            {
                page: this.page,
                size: this.itemsPerPage,
                search: this.currentSearch,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            }
        });
        this.loadAll();
    }

    clear() {
        this.page = 0;
        this.currentSearch = '';
        this.router.navigate(['/role', {
            page: this.page,
            sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
        }]);
        this.loadAll();
    }
    search (query) {
        if (!query) {
            return this.clear();
        }
        this.page = 0;
        this.currentSearch = query;
        this.router.navigate(['/role', {
            search: this.currentSearch,
            page: this.page,
            sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
        }]);
        this.loadAll();
    }
    ngOnInit() {
        this.loadAll();
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
        this.registerChangeInRoles();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId (index: number, item: Role) {
        return item.id;
    }



    registerChangeInRoles() {
        this.eventSubscriber = this.eventManager.subscribe('roleListModification', (response) => this.loadAll());
    }

    sort () {
        let result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    private onSuccess (data, headers) {
        console.log(`Success fetching roles...`);
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        // this.page = pagingParams.page;
        this.roles = data;
        for (let role of data) {
            let organisationUuid = role.organisation;
            if (organisationUuid && !(organisationUuid in this.organisations)) {
                this.organisationService.findByUuid(organisationUuid).subscribe(organisation => {
                    this.organisations[organisationUuid] = organisation;
                });
            }
            for (let userUuid of role.users) {
                if (!(userUuid in this.users)) {
                    this.userService.findByUuid(userUuid).subscribe(user => {
                        this.users[userUuid] = user;
                    });
                }
            }
        }
    }

    private onError (error) {
        this.alertService.error(error.message, null, null);
    }
}
