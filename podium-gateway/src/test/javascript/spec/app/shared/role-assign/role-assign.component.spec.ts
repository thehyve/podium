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
import { JhiLanguageService, JhiEventManager } from 'ng-jhipster';
import { AlertService } from '../../../../../../main/webapp/app/core/util/alert.service';
import { MockLanguageService } from '../../../helpers/mock-language.service';
import { BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { RoleAssignComponent } from '../../../../../../main/webapp/app/shared/role/role-assign/role-assign.component';
import { RoleService } from '../../../../../../main/webapp/app/shared/role/role.service';
import { User } from '../../../../../../main/webapp/app/shared/user/user.model';
import { Role } from '../../../../../../main/webapp/app/shared/role/role.model';
import { UserService } from '../../../../../../main/webapp/app/shared/user/user.service';
import { MockAlertService } from '../../../helpers/mock-alert.service';
import { MockPrincipal } from '../../../helpers/mock-principal.service';
import { Principal } from '../../../../../../main/webapp/app/shared/auth/principal.service';
import { MockEventManager } from '../../../helpers/mock-event-manager.service';
import { Observable } from 'rxjs';
import { PodiumTestModule } from '../../../test.module';
import { Organisation } from '../../../../../../main/webapp/app/shared/organisation/organisation.model';
import { MockTranslateService } from '../../../helpers/MockTranslateService';
import { TranslateService } from '@ngx-translate/core';

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
                BaseRequestOptions,
                MockBackend,
                UserService,
                {
                    provide: Http,
                    useFactory: (backendInstance: MockBackend, defaultOptions: BaseRequestOptions) => {
                        return new Http(backendInstance, defaultOptions);
                    },
                    deps: [MockBackend, BaseRequestOptions]
                },
                {
                    provide: TranslateService,
                    useClass: MockTranslateService
                },
                {
                    provide: JhiLanguageService,
                    useClass: MockLanguageService
                },
                {
                    provide: Principal,
                    useClass: MockPrincipal
                },
                {
                    provide: RoleService,
                    useClass: RoleService
                },
                {
                    provide: AlertService,
                    useClass: MockAlertService
                },
                {
                    provide: JhiEventManager,
                    useClass: MockEventManager
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
        mockPrincipal = fixture.debugElement.injector.get(Principal);
        spyOn(roleService, 'findAllRolesForOrganisation').and.returnValue(Observable.of(dummyRoles));
    });

    it('should not have user promises, organisation users or organisation roles', () => {
        expect(comp.usersPromises).toEqual([]);
        expect(comp.organisationUsers).toEqual([]);
        expect(comp.organisationRoles).toBe(undefined);
    });

    it('should retrieve and convert the roles with users', () => {
        comp.organisation = dummyOrganisation;
        mockPrincipal.setResponse(dummyBbmriAdmin);

        spyOn(comp, 'registerChangeInRoles');

        comp.ngOnInit();
        expect(mockPrincipal.identitySpy).toHaveBeenCalled();
        expect(comp.registerChangeInRoles).toHaveBeenCalled();
    });

    // Save
    it('should save a user to the requested role', () => {
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
