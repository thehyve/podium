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
import { JhiLanguageService, AlertService } from 'ng-jhipster';
import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ng2-bootstrap/typeahead';

import { Role } from '../role.model';
import { RoleService } from '../role.service';
import { User } from '../../../shared/user/user.model';
import { UserService } from '../../../shared/user/user.service';
import { Principal } from '../../../shared';
import { Authority } from '../../../shared/authority/authority';
import { ORGANISATION_AUTHORITIES_MAP, ORGANISATION_AUTHORITIES } from '../../../shared/authority/authority.constants';
import { OrganisationUser } from '../../user/organisation-user.model';
import { Response } from '@angular/http';

@Component({
    selector: 'pdm-role-assign',
    templateUrl: './role-assign.component.html',
    styleUrls: ['role-assign.scss']
})
export class RoleAssignComponent implements OnInit {

    currentAccount: any;
    users: { [uuid: string]: User; };

    authoritiesMap: { [token: string]: Authority; };
    authorityOptions: ReadonlyArray<Authority>;

    error: any;
    success: any;

    public typeaheadLoading: boolean;
    public typeaheadNoResults: boolean;
    public organisationRoles: Role[];
    public organisationUsers: any[] = [];

    @Input() organisation;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private roleService: RoleService,
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

        if (this.organisation) {
            this.roleService.findAllRolesForOrganisation(this.organisation.uuid).subscribe(roles => {
                this.organisationRoles = roles;
                for (let i = 0; i < roles.length; i++) {
                    let role: Role = roles[i];

                    for (let u = 0; u < role.users.length; u++) {
                        let userUUID: string = role.users[u];

                        this.userService.findByUuid(userUUID).subscribe(userRes => {
                            let organisationUser = this.generateOrganisationUser(userRes, role);
                            this.organisationUsers.push(organisationUser);
                        });
                    }
                }
            });
        }
        // Add an empty user to the organisation user array
        let organisationUser = this.generateOrganisationUser(null, null);
        this.organisationUsers.push(organisationUser);
        this.organisationUsers.reverse();
    }

    public generateOrganisationUser(user: User, role: Role) {
        let orgUser: OrganisationUser = new OrganisationUser();

        if ( user ) {
            orgUser.fullName = user.firstName + ' ' + user.lastName;
            orgUser.uuid = user.uuid;
            orgUser.searchTerm = orgUser.fullName;
        }

        if ( role ) {
            orgUser.authority = role.authority;
            orgUser.isSaved = user.uuid != null ? true : false;
        }

        orgUser.dataSource = this.getDatasourceForUser(orgUser);

        return orgUser;
    }

    getDatasourceForUser(organisationUser?: OrganisationUser): Observable<any> {
        /**
         * User typeahead datasource observable
         */
        return Observable.create((observer: any) => {
            // Runs on every search
            // each organisation user has a 'searchTerm' property that is used as the input and is bound to [(ngModel)]
            // when user types into the input .next is called with the value from the input
            observer.next({query: organisationUser.searchTerm });
        }).mergeMap((term: any) => this.userService.suggest(term));
    }

    private save(user: OrganisationUser) {
        // Find and Update role by authority
        let filteredRoles = this.organisationRoles.filter(r => r.authority === user.authority);
        let role = filteredRoles[0];

        if (role) {
            // Check if role already has user
            if (role.users.indexOf(user.uuid) > -1) {
                this.onError({message: 'Cannot add user ' + user.fullName + ' to the same role twice.'});
            } else {
                // Add user to role
                role.users.push(user.uuid);
                this.roleService.update(role)
                    .subscribe(
                        (res: Response) => this.onSaveSuccess(res),
                        (res: Response) => this.onSaveError(res.json())
                    );

            }
        } else {
            this.onError({message: 'Cannot find role for authority ' + user.authority });
        }
    }

    public changeTypeaheadNoResults(e: boolean): void {
        this.typeaheadNoResults = e;
    }

    public typeaheadOnSelect(e: TypeaheadMatch, user: OrganisationUser): void {
        user.uuid = e.item.uuid;
    }

    trackAuthorityByToken(index: number, item: string) {
        return item;
    }

    trackByUuid(index: number, item: any) {
        return item.uuid;
    }

    private onSaveSuccess(res: Response) {
        this.alertService.success('Successfully saved', res);
    }

    private onError (error) {
        this.alertService.error(error.message, null, null);
    }


    private onSaveError (error) {
        this.alertService.error(error.message, null, null);
    }
}
