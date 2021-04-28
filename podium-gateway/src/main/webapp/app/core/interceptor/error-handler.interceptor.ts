/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpErrorResponse, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

import { EventManager, EventWithContent } from '../util/event-manager.service';

@Injectable()
export class ErrorHandlerInterceptor implements HttpInterceptor {
    constructor(private eventManager: EventManager) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(
            tap({
                error: (err: HttpErrorResponse) => {
                    if (!(err.status === 401 && (err.message === '' || err.url?.includes('zapi/account')))) {
                        this.eventManager.broadcast(new EventWithContent('podiumApp.httpError', err));
                    }
                },
            })
        );
    }
}
