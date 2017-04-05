import { async, inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, Http, HttpModule, Response, ResponseOptions } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { RequestService } from '../../../../../../main/webapp/app/shared/request/request.service';

describe('RequestService (Mocked)', () => {
    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                RequestService,

                MockBackend,
                BaseRequestOptions,
                {
                    provide: Http,
                    useFactory: (backend, options) => new Http(backend, options),
                    deps: [MockBackend, BaseRequestOptions]
                }
            ],
            imports: [
                HttpModule
            ]
        });
    });

    it('should construct', async(inject(
        [RequestService, MockBackend], (service, mockBackend) => {

            expect(service).toBeDefined();
        })));

    describe('someMethod', () => {
        const mockResponse = [ {
            "id" : 999,
            "uuid" : "xxx",
            "requester" : "xxx",
            "status" : "Review",
        } ];

        it('should parse response', async(inject(
            [RequestService, MockBackend], (service, mockBackend) => {

                mockBackend.connections.subscribe(conn => {
                    conn.mockRespond(new Response(new ResponseOptions({ body: JSON.stringify(mockResponse) })));
                });

                const result = service.findSubmittedRequest();

                result.subscribe(res => {
                    expect(res).toEqual([{
                        "id" : 999,
                        "uuid" : "xxx",
                        "requester" : "xxx",
                        "status" : "Review",
                    }] );
                });
            })));
    });
});
