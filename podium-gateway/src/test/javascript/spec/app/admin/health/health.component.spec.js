"use strict";
var testing_1 = require('@angular/core/testing');
var testing_2 = require('@angular/http/testing');
var http_1 = require('@angular/http');
var ng_bootstrap_1 = require('@ng-bootstrap/ng-bootstrap');
var ng_jhipster_1 = require('ng-jhipster');
var mock_language_service_1 = require('../../../helpers/mock-language.service');
var health_component_1 = require('../../../../../../main/webapp/app/admin/health/health.component');
var health_service_1 = require('../../../../../../main/webapp/app/admin/health/health.service');
describe('Component Tests', function () {
    describe('JhiHealthCheckComponent', function () {
        var comp;
        var fixture;
        var service;
        beforeEach(testing_1.async(function () {
            testing_1.TestBed.configureTestingModule({
                declarations: [health_component_1.JhiHealthCheckComponent],
                providers: [
                    testing_2.MockBackend,
                    http_1.BaseRequestOptions,
                    {
                        provide: http_1.Http,
                        useFactory: function (backendInstance, defaultOptions) {
                            return new http_1.Http(backendInstance, defaultOptions);
                        },
                        deps: [testing_2.MockBackend, http_1.BaseRequestOptions]
                    },
                    health_service_1.JhiHealthService,
                    {
                        provide: ng_jhipster_1.JhiLanguageService,
                        useClass: mock_language_service_1.MockLanguageService
                    },
                    {
                        provide: ng_bootstrap_1.NgbModal,
                        useValue: null
                    }
                ]
            })
                .overrideComponent(health_component_1.JhiHealthCheckComponent, {
                set: {
                    template: ''
                }
            })
                .compileComponents();
        }));
        beforeEach(function () {
            fixture = testing_1.TestBed.createComponent(health_component_1.JhiHealthCheckComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(health_service_1.JhiHealthService);
        });
        describe('baseName and subSystemName', function () {
            it('should return the basename when it has no sub system', function () {
                expect(comp.baseName('base')).toBe('base');
            });
            it('should return the basename when it has sub systems', function () {
                expect(comp.baseName('base.subsystem.system')).toBe('base');
            });
            it('should return the sub system name', function () {
                expect(comp.subSystemName('subsystem')).toBe('');
            });
            it('should return the subsystem when it has multiple keys', function () {
                expect(comp.subSystemName('subsystem.subsystem.system')).toBe(' - subsystem.system');
            });
        });
        describe('transformHealthData', function () {
            it('should flatten empty health data', function () {
                var data = {};
                var expected = [];
                expect(service.transformHealthData(data)).toEqual(expected);
            });
        });
        it('should flatten health data with no subsystems', function () {
            var data = {
                'status': 'UP',
                'db': {
                    'status': 'UP',
                    'database': 'H2',
                    'hello': '1'
                },
                'mail': {
                    'status': 'UP',
                    'error': 'mail.a.b.c'
                }
            };
            var expected = [
                {
                    'name': 'db',
                    'error': undefined,
                    'status': 'UP',
                    'details': {
                        'database': 'H2',
                        'hello': '1'
                    }
                },
                {
                    'name': 'mail',
                    'error': 'mail.a.b.c',
                    'status': 'UP'
                }
            ];
            expect(service.transformHealthData(data)).toEqual(expected);
        });
        it('should flatten health data with subsystems at level 1, main system has no additional information', function () {
            var data = {
                'status': 'UP',
                'db': {
                    'status': 'UP',
                    'database': 'H2',
                    'hello': '1'
                },
                'mail': {
                    'status': 'UP',
                    'error': 'mail.a.b.c'
                },
                'system': {
                    'status': 'DOWN',
                    'subsystem1': {
                        'status': 'UP',
                        'property1': 'system.subsystem1.property1'
                    },
                    'subsystem2': {
                        'status': 'DOWN',
                        'error': 'system.subsystem1.error',
                        'property2': 'system.subsystem2.property2'
                    }
                }
            };
            var expected = [
                {
                    'name': 'db',
                    'error': undefined,
                    'status': 'UP',
                    'details': {
                        'database': 'H2',
                        'hello': '1'
                    }
                },
                {
                    'name': 'mail',
                    'error': 'mail.a.b.c',
                    'status': 'UP'
                },
                {
                    'name': 'system.subsystem1',
                    'error': undefined,
                    'status': 'UP',
                    'details': {
                        'property1': 'system.subsystem1.property1'
                    }
                },
                {
                    'name': 'system.subsystem2',
                    'error': 'system.subsystem1.error',
                    'status': 'DOWN',
                    'details': {
                        'property2': 'system.subsystem2.property2'
                    }
                }
            ];
            expect(service.transformHealthData(data)).toEqual(expected);
        });
        it('should flatten health data with subsystems at level 1, main system has additional information', function () {
            var data = {
                'status': 'UP',
                'db': {
                    'status': 'UP',
                    'database': 'H2',
                    'hello': '1'
                },
                'mail': {
                    'status': 'UP',
                    'error': 'mail.a.b.c'
                },
                'system': {
                    'status': 'DOWN',
                    'property1': 'system.property1',
                    'subsystem1': {
                        'status': 'UP',
                        'property1': 'system.subsystem1.property1'
                    },
                    'subsystem2': {
                        'status': 'DOWN',
                        'error': 'system.subsystem1.error',
                        'property2': 'system.subsystem2.property2'
                    }
                }
            };
            var expected = [
                {
                    'name': 'db',
                    'error': undefined,
                    'status': 'UP',
                    'details': {
                        'database': 'H2',
                        'hello': '1'
                    }
                },
                {
                    'name': 'mail',
                    'error': 'mail.a.b.c',
                    'status': 'UP'
                },
                {
                    'name': 'system',
                    'error': undefined,
                    'status': 'DOWN',
                    'details': {
                        'property1': 'system.property1'
                    }
                },
                {
                    'name': 'system.subsystem1',
                    'error': undefined,
                    'status': 'UP',
                    'details': {
                        'property1': 'system.subsystem1.property1'
                    }
                },
                {
                    'name': 'system.subsystem2',
                    'error': 'system.subsystem1.error',
                    'status': 'DOWN',
                    'details': {
                        'property2': 'system.subsystem2.property2'
                    }
                }
            ];
            expect(service.transformHealthData(data)).toEqual(expected);
        });
        it('should flatten health data with subsystems at level 1, main system has additional error', function () {
            var data = {
                'status': 'UP',
                'db': {
                    'status': 'UP',
                    'database': 'H2',
                    'hello': '1'
                },
                'mail': {
                    'status': 'UP',
                    'error': 'mail.a.b.c'
                },
                'system': {
                    'status': 'DOWN',
                    'error': 'show me',
                    'subsystem1': {
                        'status': 'UP',
                        'property1': 'system.subsystem1.property1'
                    },
                    'subsystem2': {
                        'status': 'DOWN',
                        'error': 'system.subsystem1.error',
                        'property2': 'system.subsystem2.property2'
                    }
                }
            };
            var expected = [
                {
                    'name': 'db',
                    'error': undefined,
                    'status': 'UP',
                    'details': {
                        'database': 'H2',
                        'hello': '1'
                    }
                },
                {
                    'name': 'mail',
                    'error': 'mail.a.b.c',
                    'status': 'UP'
                },
                {
                    'name': 'system',
                    'error': 'show me',
                    'status': 'DOWN'
                },
                {
                    'name': 'system.subsystem1',
                    'error': undefined,
                    'status': 'UP',
                    'details': {
                        'property1': 'system.subsystem1.property1'
                    }
                },
                {
                    'name': 'system.subsystem2',
                    'error': 'system.subsystem1.error',
                    'status': 'DOWN',
                    'details': {
                        'property2': 'system.subsystem2.property2'
                    }
                }
            ];
            expect(service.transformHealthData(data)).toEqual(expected);
        });
    });
});
