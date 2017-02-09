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

describe('Component Tests', () => {

    describe('Role Management Detail Component', () => {
        let comp: RoleDetailComponent;
        let fixture: ComponentFixture<RoleDetailComponent>;
        let service: RoleService;

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
            service = fixture.debugElement.injector.get(RoleService);
        });


        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN
            spyOn(service, 'find').and.returnValue(Observable.of(new Role(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith(123);
            expect(comp.role).toEqual(jasmine.objectContaining({id: 10}));
            });
        });
    });

});
