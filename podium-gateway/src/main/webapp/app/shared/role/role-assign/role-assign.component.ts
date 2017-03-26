/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventManager, JhiLanguageService, AlertService } from 'ng-jhipster';

import { Role } from '../role.model';
import { RoleService } from '../role.service';
import { User } from '../../../shared/user/user.model';
import { UserService } from '../../../shared/user/user.service';
import { Principal } from '../../../shared';
import { Authority } from '../../../shared/authority/authority';
import { ORGANISATION_AUTHORITIES_MAP, ORGANISATION_AUTHORITIES } from '../../../shared/authority/authority.constants';
import { Organisation, OrganisationService } from '../../../backoffice/modules/organisation';

import { Observable } from 'rxjs';

import { TypeaheadMatch } from 'ng2-bootstrap/typeahead';

@Component({
    selector: 'pdm-role-assign',
    templateUrl: './role-assign.component.html'
})
export class RoleAssignComponent implements OnInit, OnDestroy {

    currentAccount: any;
    users: { [uuid: string]: User; };

    authoritiesMap: { [token: string]: Authority; };
    authorityOptions: ReadonlyArray<Authority>;

    error: any;
    success: any;

    public asyncSelected: string;
    public typeaheadLoading: boolean;
    public typeaheadNoResults: boolean;
    public dataSource: Observable<any>;

    @Input() organisation;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private roleService: RoleService,
        private organisationService: OrganisationService,
        private userService: UserService,
        private alertService: AlertService,
        private principal: Principal,

    ) {
        this.jhiLanguageService.setLocations(['organisation', 'role']);
        this.authoritiesMap = ORGANISATION_AUTHORITIES_MAP;
        this.authorityOptions = ORGANISATION_AUTHORITIES;
        this.users = {};
    }

    ngOnInit() {
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });

        // Fetch roles for existing organisation
        if (this.organisation.uuid) {
            this.organisation = this.organisation.roles;
        } else {
            let role = new Role;
            role.organisation = '';
            this.organisation.roles.push(role);
            console.log('Pushed role ', this.organisation);
        }

        this.dataSource = Observable.create((observer: any) => {
            // Runs on every search
            // asyncSelected is a component variable bound to [(ngModel)]
            // when user types into the input .next is called with the value from the input
            console.log('Async ', this.asyncSelected);
            observer.next({query: this.asyncSelected});
        }).mergeMap((term: any) => this.userService.search(term));
    }

    ngOnDestroy() {
        // this.eventManager.destroy(this.eventSubscriber);
    }

    trackId (index: number, item: Role) {
        return item.id;
    }

    public changeTypeaheadLoading(e: boolean): void {
        this.typeaheadLoading = e;
    }

    public changeTypeaheadNoResults(e: boolean): void {
        this.typeaheadNoResults = e;
    }

    public typeaheadOnSelect(e: TypeaheadMatch): void {
        console.log('Selected value: ', e.value);
    }


    /*private onSuccess (data, headers) {
        console.log(`Success fetching roles...`);

        this.roles = data;
        for (let role of data) {
            let organisationUuid = role.organisation;
            if (organisationUuid && !(organisationUuid in this.organisations)) {
                this.organisationService.findByUuid(organisationUuid).subscribe(organisation => {
                    this.organisations[organisationUuid] = organisation;
                });
            }

            if (role) {
                for (let userUuid of role.users) {
                    if (!(userUuid in this.users)) {
                        this.userService.findByUuid(userUuid).subscribe(user => {
                            this.users[userUuid] = user;
                        });
                    }
                }
            }

        }
    }*/

    private onError (error) {
        this.alertService.error(error.message, null, null);
    }
}
