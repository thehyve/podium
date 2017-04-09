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
import { JhiLanguageService, AlertService, EventManager } from 'ng-jhipster';
import { Observable, Subscription } from 'rxjs';
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
export class RoleAssignComponent implements OnInit, OnDestroy {

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
    private eventSubscriber: Subscription;
    public usersPromises: Promise<Response>[] = [];

    @Input() organisation;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private roleService: RoleService,
        private userService: UserService,
        private alertService: AlertService,
        private principal: Principal,
        private eventManager: EventManager
    ) {
        this.jhiLanguageService.setLocations(['organisation', 'role']);
        this.authoritiesMap = ORGANISATION_AUTHORITIES_MAP;
        this.authorityOptions = ORGANISATION_AUTHORITIES;
        this.users = {};
    }

    ngOnInit() {
        this.principal.identity().then((account: User) => {
            this.currentAccount = account;
        });

        this.registerChangeInRoles();
        this.eventManager.broadcast({ name: 'userRolesModification', content: 'OK'});
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInRoles() {
        this.eventSubscriber = this.eventManager.subscribe('userRolesModification', (response) => {
            this.loadAllRolesForOrganisation().subscribe(() => {
                console.log('Finished Loading all roles.');

                Promise.all(this.usersPromises)
                    .then(() => {
                        this.addNewOrganisationUser();
                    });
            });
        });
    }

    public generateOrganisationUser(user: User, role: Role) {
        let orgUser: OrganisationUser = new OrganisationUser();

        if ( user ) {
            orgUser.fullName = user.firstName + ' ' + user.lastName;
            orgUser.uuid = user.uuid;
            orgUser.searchTerm = orgUser.fullName;
        }

        if ( role ) {
            orgUser.previousAuthority = role.authority;
            orgUser.authority = role.authority;
            orgUser.isSaved = user.uuid != null;
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

    public save(user: OrganisationUser) {
        // Find and Update role by authority
        let role = this.getRoleByAuthority(user.authority);

        // Check if role already has user
        this.updateRole(role, user, false).subscribe(
            (res) => { this.onSaveSuccess(res, false); },
            (err) => { this.onError(err); }
        );
    }

    public update(user: OrganisationUser) {
        // Remove previous role
        let previousRole = this.getRoleByAuthority(user.previousAuthority);

        this.updateRole(previousRole, user, true).subscribe(
            (previousRes: Response) => {
              // Add new role
              let role = this.getRoleByAuthority(user.authority);
              this.updateRole(role, user, false).subscribe(
                  (res: Response) => { this.onSaveSuccess(res, false); },
                  (err: Response) => { this.onError(err); },
              );
        },
        (err: Response) => { this.onError(err); }
        );
    }

    public delete(user: OrganisationUser) {
        // Find and update role by authority
        let role = this.getRoleByAuthority(user.authority);

        this.updateRole(role, user, true).subscribe(
            (res: Response) => { this.onSaveSuccess(res, true); },
            (err: Response) => { this.onError(err); }
        );
    }

    private loadAllRolesForOrganisation(): Observable<any> {
        return Observable.create((observer: any) => {

            this.organisationUsers = [];
            if (this.organisation) {
                this.roleService.findAllRolesForOrganisation(this.organisation.uuid).subscribe(roles => {
                    this.organisationRoles = roles;
                    for (let i = 0; i < roles.length; i++) {
                        let role: Role = roles[i];

                        for (let u = 0; u < role.users.length; u++) {
                            let userUUID: string = role.users[u];

                            let promise: Promise<Response> = this.userService.findByUuid(userUUID).toPromise();
                                /*.subscribe(userRes => {
                                let organisationUser = this.generateOrganisationUser(userRes, role);
                                this.organisationUsers.push(organisationUser);
                            })*/

                            promise.then(userRes => {
                                let organisationUser = this.generateOrganisationUser(userRes, role);
                                this.organisationUsers.push(organisationUser);
                            });

                            this.usersPromises.push(promise);
                        }
                    }
                    observer.next();
                });
            } else {
                observer.next();
            }
        });
    }

    private getRoleByAuthority(authority: string): Role {
        // Find and Update role by authority
        let filteredRoles = this.organisationRoles.filter(r => r.authority === authority);
        return filteredRoles[0];
    }

    private updateRole(role: Role, user: OrganisationUser, remove: boolean) {
        return Observable.create((observer) => {
            if (role) {
                let userIdx = role.users.indexOf(user.uuid);

                // Add / remove user in role
                if (remove) {
                    role.users.splice(userIdx, 1);
                } else {
                    // Check if role already has user
                    if (userIdx > -1) {
                        this.onError({message: 'Cannot add user ' + user.fullName + ' to the same role twice.'});
                    } else {
                        role.users.push(user.uuid);
                    }
                }

                // Perform update
                this.roleService.update(role)
                    .subscribe(
                        (res: Response) => {
                            observer.next(res);
                        },
                        (res: Response) => {
                            return Observable.throw(res.json());
                        }
                    );
            } else {
                this.onError({message: 'Cannot find role for authority ' + user.authority });
            }
        });
    }

    private onSaveSuccess(res: Response, isDelete: boolean) {
        let notification = isDelete ? 'podiumGatewayApp.roleAssign.deleted' : 'podiumGatewayApp.roleAssign.saved';
        this.alertService.success(notification);
        this.eventManager.broadcast({ name: 'userRolesModification', content: 'OK'});
    }

    private onError (error) {
        this.alertService.error(error.message, null, null);
    }

    private onSaveError (error) {
        this.alertService.error(error.message, null, null);
    }

    private addNewOrganisationUser() {
        // Add an empty user to the organisation user array
        let organisationUser = this.generateOrganisationUser(null, null);
        this.organisationUsers.push(organisationUser);
    }

    /**
     * Template features
     */
    public userAuthorityChange(orgUser: OrganisationUser, event: any) {
        orgUser.isDirty = false;
        if ((orgUser.authority !== orgUser.previousAuthority) && orgUser.isSaved) {
            orgUser.isDirty = true;
        }
    }

    public canAdd(orgUser: OrganisationUser, currentUser: User): boolean {
        if (orgUser.uuid && orgUser.previousAuthority !== orgUser.authority && !orgUser.isSaved) {
            return true;
        }

        return false;
    }

    public canRemove(orgUser: OrganisationUser, currentUser: User): boolean {
        if (!orgUser || !orgUser.isSaved) {
            return false;
        }
        if (orgUser.uuid === currentUser.uuid && orgUser.authority === 'ROLE_ORGANISATION_ADMIN' && orgUser.isSaved) {
            return false;
        }

        return !!orgUser.uuid;
    }

    public canUpdate(orgUser: OrganisationUser, currentUser: User): boolean {
        if (!orgUser) {
            return false;
        }

        if (orgUser.uuid === currentUser.uuid && orgUser.authority === 'ROLE_ORGANISATION_ADMIN') {
            return false;
        }

        if (orgUser.isDirty) {
            return true;
        }

        return orgUser.uuid && orgUser.isSaved && orgUser.isDirty;
    }

    public isDisabled(orgUser: OrganisationUser, currentUser: User): boolean {
        if (orgUser.isSaved) {
            return true;
        }

        return currentUser.uuid === orgUser.uuid && orgUser.authority === 'ROLE_ORGANISATION_ADMIN';
    }

    public isAdminOfOrganisation(orgUser: OrganisationUser, currentUser: User): boolean {
        if (currentUser.uuid === orgUser.uuid && orgUser.authority === 'ROLE_ORGANISATION_ADMIN') {
            return true;
        }

        return false;
    }

    public typeaheadOnSelect(e: TypeaheadMatch, user: OrganisationUser): void {
        user.uuid = e.item.uuid;
    }

    public trackAuthorityByToken(index: number, item: string) {
        return item;
    }

    public trackByUuid(index: number, item: any) {
        return item.uuid;
    }

    public changeTypeaheadNoResults(e: boolean): void {
        this.typeaheadNoResults = e;
    }
}
