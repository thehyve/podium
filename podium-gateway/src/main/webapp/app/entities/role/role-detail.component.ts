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
import { ActivatedRoute } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { Role } from './role.model';
import { RoleService } from './role.service';
import {Organisation} from '../organisation/organisation.model';
import {User} from '../../shared/user/user.model';
import {OrganisationService} from '../organisation/organisation.service';
import {UserService} from '../../shared/user/user.service';
import {Authority} from "../../shared/authority/authority";
import {AUTHORITIES_MAP} from "../../shared/authority/authority.constants";

@Component({
    selector: 'jhi-role-detail',
    templateUrl: './role-detail.component.html',
    providers: [OrganisationService, UserService]
})
export class RoleDetailComponent implements OnInit, OnDestroy {

    role: Role;
    organisation: Organisation;
    users: { [uuid: string]: User; };
    authoritiesMap: { [token: string]: Authority; };
    private subscription: any;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private roleService: RoleService,
        private organisationService: OrganisationService,
        private userService: UserService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['role']);
        this.authoritiesMap = AUTHORITIES_MAP;
        this.users = {};
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe(params => {
            this.load(params['id']);
        });
    }

    load(id) {
        this.roleService.find(id).subscribe(role => {
            this.role = role;
            if (role.organisation) {
                this.organisationService.findByUuid(role.organisation).subscribe(organisation => {
                    this.organisation = organisation;
                });
            }
            for (let userUuid of role.users) {
                if (!(userUuid in this.users)) {
                    this.userService.findByUuid(userUuid).subscribe(user => {
                        this.users[userUuid] = user;
                    });
                }
            }
        });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

}
