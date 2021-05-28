/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Observable, of } from 'rxjs';

import { PodiumTestModule } from '../../test/test.module';
import { AccountService } from '../../../core/auth/account.service';
import { EventManager } from '../../../core/util/event-manager.service';
import { Organisation } from '../../organisation/organisation.model';
import { User } from '../../user/user.model';
import { UserService } from '../../user/user.service';
import { Role } from '../role.model';
import { RoleService } from '../role.service';
import { RoleAssignComponent } from './role-assign.component';

describe('RoleAssignComponent', () => {

    let comp: RoleAssignComponent;
    let fixture: ComponentFixture<RoleAssignComponent>;
    let roleService: RoleService;
    let mockPrincipal: any;

    let dummyOrganisation = new Organisation({
        id: 90,
        name: 'dummy',
        uuid: 'dummy-orgnisation-uuid'
    });

    let dummyBbmriAdmin: User = {
        id: 1,
        uuid: 'dummy-uuid-1',
        authorities : [ 'ROLE_BBMRI_ADMIN', 'ROLE_ORGANISATION_ADMIN' ],
        firstName: 'Dummy BBMRI',
        lastName: 'Admin'
    };

    let dummyOrganisationAdmin: User = {
        id: 2,
        uuid: 'dummy-uuid-2',
        authorities : [ 'ROLE_ORGANISATION_ADMIN' ],
        firstName: 'Dummy Organisation',
        lastName: 'Admin'
    };

    let dummyRoles: Role[] = [
        {
            'id' : 3,
            'organisation' : '12dd08b3-eb8b-476e-a0b3-716cb6b5df7a',
            'authority' : 'ROLE_ORGANISATION_ADMIN',
            'users' : [ 'dummy-uuid-1', 'dummy-uuid-2' ]
        }, {
            'id' : 4,
            'organisation' : '12dd08b3-eb8b-476e-a0b3-716cb6b5df7a',
            'authority' : 'ROLE_ORGANISATION_COORDINATOR',
            'users' : [ ]
        }, {
            'id': 5,
            'organisation': '12dd08b3-eb8b-476e-a0b3-716cb6b5df7a',
            'authority': 'ROLE_REVIEWER',
            'users': []
        }];

    // async beforeEach, since we use external templates & styles
    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [
                FormsModule,
                PodiumTestModule
            ],
            providers: [
                RoleService,
                UserService,
                {
                    provide: EventManager,
                    useValue: {
                        broadcast: () => {},
                        destroy: () => {},
                    }
                }
            ],
            declarations: [RoleAssignComponent],
        }).overrideTemplate(RoleAssignComponent, '')
            .compileComponents();
    }));

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(RoleAssignComponent);
        comp = fixture.componentInstance;
        roleService = fixture.debugElement.injector.get(RoleService);
        mockPrincipal = fixture.debugElement.injector.get(AccountService);
        spyOn(roleService, 'findAllRolesForOrganisation').and.returnValue(of(dummyRoles));
    });

    it('should not have user promises, organisation users or organisation roles', () => {
        expect(comp.usersPromises).toEqual([]);
        expect(comp.organisationUsers).toEqual([]);
        expect(comp.organisationRoles).toBe(undefined);
    });

    it('should retrieve and convert the roles with users', () => {
        comp.organisation = dummyOrganisation;
        mockPrincipal.mockIdentity(of(dummyBbmriAdmin));

        spyOn(comp, 'registerChangeInRoles');

        comp.ngOnInit();
        expect(mockPrincipal.identity).toHaveBeenCalled();
        expect(comp.registerChangeInRoles).toHaveBeenCalled();
    });

    // Save
    it('should save a user to the requested role', () => {
        let role = dummyRoles[1];
        let orgAdminUser = comp.generateOrganisationUser(dummyOrganisationAdmin, role);
        comp.organisationRoles = dummyRoles;

        // Set spies
        spyOn(comp, 'updateRole').and.returnValue(
            new Observable((observer) => {
                observer.next();
            })
        );
        spyOn(comp, 'getRoleByAuthority').and.returnValue(role);

        comp.save(orgAdminUser);
        expect(comp.getRoleByAuthority).toHaveBeenCalled();
        expect(comp.updateRole).toHaveBeenCalledWith(role, orgAdminUser, false);
    });

    // Update COORDINATOR - > REVIEWER
    it('should update a user role', () => {
        let role = dummyRoles[1];

        let orgAdminUser = comp.generateOrganisationUser(dummyOrganisationAdmin, role);
        orgAdminUser.previousAuthority = 'ROLE_ORGANISATION_COORDINATOR';
        orgAdminUser.authority = 'ROLE_REVIEWER';
        comp.organisationRoles = dummyRoles;

        // Set spies
        spyOn(comp, 'getRoleByAuthority').and.returnValue(role);
        spyOn(comp, 'updateRole').and.callThrough();

        comp.update(orgAdminUser);
        expect(comp.getRoleByAuthority).toHaveBeenCalledWith('ROLE_ORGANISATION_COORDINATOR');
        expect(comp.updateRole).toHaveBeenCalledWith(role, orgAdminUser, true);
    });

    // Delete
    it('should remove a user from a role', () => {
        let role = dummyRoles[1];
        let orgAdminUser = comp.generateOrganisationUser(dummyOrganisationAdmin, role);
        comp.organisationRoles = dummyRoles;

        // Set spies
        spyOn(comp, 'updateRole').and.returnValue(
            Observable.create((observer) => {
                observer.next();
            })
        );
        spyOn(comp, 'getRoleByAuthority').and.returnValue(role);

        comp.delete(orgAdminUser);
        expect(comp.getRoleByAuthority).toHaveBeenCalled();
        expect(comp.updateRole).toHaveBeenCalledWith(role, orgAdminUser, true);
    });

    // Load all roles for organisation
    it('should retrieve all roles for an organisation', () => {
        comp.organisation = dummyOrganisation;
        comp.loadAllRolesForOrganisation().subscribe(() => {
            expect(comp.organisationRoles).toEqual(dummyRoles);
            expect(comp.usersPromises.length).toEqual(2);
        });
    });

    // Get role by authority
    it('should get the organisation role by authority', () => {
        comp.organisationRoles = dummyRoles;

        let authority = 'ROLE_ORGANISATION_ADMIN';
        let role: Role = comp.getRoleByAuthority(authority);
        expect(role).toEqual(dummyRoles[0]);
    });

    // Can add
    it('should be able to assign a new user to a role', () => {
        let role = dummyRoles[0];
        let bbmriOrgUser = comp.generateOrganisationUser(dummyBbmriAdmin, role);
        bbmriOrgUser.authority = 'ROLE_ORGANISATION_REVIEWER';
        bbmriOrgUser.isSaved = false;

        let canAdd = comp.canAdd(bbmriOrgUser);
        expect(canAdd).toBeTruthy();
    });

    // Can not add
    it('should not be able to assign a user with the same role', () => {
        let role = dummyRoles[0];
        let bbmriOrgUser = comp.generateOrganisationUser(dummyBbmriAdmin, role);

        let canNotAdd = comp.canAdd(bbmriOrgUser);
        expect(canNotAdd).toBeFalsy();
    });

    // Can remove
    it('should be able to remove a user from a role', () => {
        let role = dummyRoles[0];
        let bbmriOrgUser = comp.generateOrganisationUser(dummyBbmriAdmin, role);
        bbmriOrgUser.authority = 'ROLE_ORGANISATION_REVIWER';

        let canRemove = comp.canRemove(bbmriOrgUser, dummyBbmriAdmin);
        expect(canRemove).toBeTruthy();
    });

    // Cannot remove
    it('should not be able to remove a user from a role', () => {
        let role = dummyRoles[0];
        delete dummyBbmriAdmin.uuid;

        let bbmriOrgUser = comp.generateOrganisationUser(dummyBbmriAdmin, role);
        let canRemove = comp.canRemove(bbmriOrgUser, dummyBbmriAdmin);
        expect(canRemove).toBeFalsy();
    });

    // Can update
    it('should be able to update a user in a role', () => {
        let role = dummyRoles[2];
        let bbmriOrgUser = comp.generateOrganisationUser(dummyBbmriAdmin, role);
        bbmriOrgUser.isDirty = true;

        let canUpdate = comp.canUpdate(bbmriOrgUser, dummyBbmriAdmin);
        expect(canUpdate).toBeTruthy();
    });

    // Is not disabled
    it('should not disable a input when the user has not been saved', () => {
        let role = dummyRoles[1];

        let bbmriOrgUser = comp.generateOrganisationUser(dummyBbmriAdmin, role);
        let isDisabled = comp.isDisabled(bbmriOrgUser, dummyBbmriAdmin);

        expect(isDisabled).toBeFalsy();
    });

    // Is disabled
    it('should disable a input when the user is saved and is an Organisation Admin', () => {
        let role = dummyRoles[0];
        delete dummyBbmriAdmin.uuid;

        let bbmriOrgUser = comp.generateOrganisationUser(dummyBbmriAdmin, role);
        let isDisabled = comp.isDisabled(bbmriOrgUser, dummyBbmriAdmin);
        expect(isDisabled).toBeTruthy();
    });

    // Is admin of organisation
    it('should truthfully indicate whether the current user is the admin of an organisation', () => {
        let role = dummyRoles[0];
        let bbmriOrgUser = comp.generateOrganisationUser(dummyBbmriAdmin, role);

        let isAdminOfOrganisation = comp.isAdminOfOrganisation(bbmriOrgUser, dummyBbmriAdmin);
        expect(isAdminOfOrganisation).toBeTruthy();
    });

});
