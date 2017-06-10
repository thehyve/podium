/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { HttpInterceptor, EventManager } from 'ng-jhipster';
import { RequestOptionsArgs, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

export class ErrorHandlerInterceptor extends HttpInterceptor {

    constructor(private eventManager: EventManager) {
        super();
    }

    requestIntercept(options?: RequestOptionsArgs): RequestOptionsArgs {
        return options;
    }

    responseIntercept(observable: Observable<Response>): Observable<Response> {
        return <Observable<Response>> observable.catch(error => {
            if (!(error.status === 401 && (error.text() === '' ||
                (error.json().path && error.json().path.indexOf('/api/account') === 0 )))) {
                this.eventManager.broadcast( {name: 'podiumGatewayApp.httpError', content: error});
            }
            return Observable.throw(error);
        });
    }
}
