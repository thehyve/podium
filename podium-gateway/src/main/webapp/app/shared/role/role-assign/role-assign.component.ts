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
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager, JhiLanguageService, AlertService } from 'ng-jhipster';

import { Role } from '../role.model';
import { RoleService } from '../role.service';
import { User } from '../../../shared/user/user.model';
import { UserService } from '../../../shared/user/user.service';
import { Principal } from '../../../shared';
import { Authority } from '../../../shared/authority/authority';
import { ORGANISATION_AUTHORITIES_MAP} from '../../../shared/authority/authority.constants';
import { Organisation, OrganisationService } from '../../../backoffice/modules/organisation';

@Component({
    selector: 'pdm-role-assign',
    templateUrl: './role-assign.component.html'
})
export class RoleAssignComponent implements OnInit, OnDestroy {

    currentAccount: any;
    roles: Role[];
    organisations: { [uuid: string]: Organisation; };
    users: { [uuid: string]: User; };
    authoritiesMap: { [token: string]: Authority; };

    error: any;
    success: any;
    eventSubscriber: Subscription;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private roleService: RoleService,
        private organisationService: OrganisationService,
        private userService: UserService,
        private alertService: AlertService,
        private principal: Principal,
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private eventManager: EventManager,
    ) {
        this.jhiLanguageService.setLocations(['role']);
        this.authoritiesMap = ORGANISATION_AUTHORITIES_MAP;
        this.organisations = {};
        this.users = {};
    }

    ngOnInit() {
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId (index: number, item: Role) {
        return item.id;
    }

    private onSuccess (data, headers) {
        console.log(`Success fetching roles...`);
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
