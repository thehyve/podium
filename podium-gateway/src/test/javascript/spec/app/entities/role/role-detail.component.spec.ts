import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { Http, BaseRequestOptions } from '@angular/http';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { DateUtils, DataUtils } from 'ng-jhipster';
import { JhiLanguageService } from 'ng-jhipster';
import { MockLanguageService } from '../../../helpers/mock-language.service';
import { MockActivatedRoute } from '../../../helpers/mock-route.service';
import { RoleDetailComponent } from '../../../../../../main/webapp/app/entities/role/role-detail.component';
import { RoleService } from '../../../../../../main/webapp/app/entities/role/role.service';
import { Role } from '../../../../../../main/webapp/app/entities/role/role.model';
import { Organisation, OrganisationService } from '../../../../../../main/webapp/app/entities/organisation';
import { User, UserService } from '../../../../../../main/webapp/app/shared/user';

describe('Component Tests', () => {

    describe('Role Management Detail Component', () => {
        let comp: RoleDetailComponent;
        let fixture: ComponentFixture<RoleDetailComponent>;
        let roleService: RoleService;
        let organisationService: OrganisationService;
        let userService: UserService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                declarations: [RoleDetailComponent],
                providers: [
                    MockBackend,
                    BaseRequestOptions,
                    DateUtils,
                    DataUtils,
                    DatePipe,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({id: 123})
                    },
                    {
                        provide: Http,
                        useFactory: (backendInstance: MockBackend, defaultOptions: BaseRequestOptions) => {
                            return new Http(backendInstance, defaultOptions);
                        },
                        deps: [MockBackend, BaseRequestOptions]
                    },
                    {
                        provide: JhiLanguageService,
                        useClass: MockLanguageService
                    },
                    RoleService
                ]
            }).overrideComponent(RoleDetailComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(RoleDetailComponent);
            comp = fixture.componentInstance;
            roleService = fixture.debugElement.injector.get(RoleService);
            organisationService = fixture.debugElement.injector.get(OrganisationService);
            userService = fixture.debugElement.injector.get(UserService);
        });


        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN
            spyOn(roleService, 'find')
                .and.returnValue(
                    Observable.of(new Role(10, 'uuidOrg', 'ROLE_ORGANISATION_ADMIN', ['uuidUser_1', 'uuidUser_2']))
                );
            spyOn(organisationService, 'findByUuid').and.returnValue(Observable.of(new Organisation(10)));
            spyOn(userService, 'findByUuid').and.returnValue(Observable.of(new User(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(roleService.find).toHaveBeenCalledWith(123);
            expect(organisationService.findByUuid).toHaveBeenCalledWith('uuidOrg');
            expect(userService.findByUuid).toHaveBeenCalledWith('uuidUser_1');
            expect(comp.role).toEqual(jasmine.objectContaining({id: 10}));
            });
        });
    });

});
