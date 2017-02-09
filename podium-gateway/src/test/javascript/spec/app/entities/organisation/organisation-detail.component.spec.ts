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
import { OrganisationDetailComponent } from '../../../../../../main/webapp/app/entities/organisation/organisation-detail.component';
import { OrganisationService } from '../../../../../../main/webapp/app/entities/organisation/organisation.service';
import { Organisation } from '../../../../../../main/webapp/app/entities/organisation/organisation.model';

describe('Component Tests', () => {

    describe('Organisation Management Detail Component', () => {
        let comp: OrganisationDetailComponent;
        let fixture: ComponentFixture<OrganisationDetailComponent>;
        let service: OrganisationService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                declarations: [OrganisationDetailComponent],
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
                    OrganisationService
                ]
            }).overrideComponent(OrganisationDetailComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(OrganisationDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(OrganisationService);
        });


        describe('OnInit', () => {
            it('Should call load all on init', () => {
            // GIVEN
            spyOn(service, 'find').and.returnValue(Observable.of(new Organisation(10)));

            // WHEN
            comp.ngOnInit();

            // THEN
            expect(service.find).toHaveBeenCalledWith(123);
            expect(comp.organisation).toEqual(jasmine.objectContaining({id: 10}));
            });
        });
    });

});
