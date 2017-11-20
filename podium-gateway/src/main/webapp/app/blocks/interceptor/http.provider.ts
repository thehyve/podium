/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Injector } from '@angular/core';
import { Http, XHRBackend, RequestOptions } from '@angular/http';
import { JhiEventManager, JhiInterceptableHttp } from 'ng-jhipster';
import { AuthInterceptor } from './auth.interceptor';
import { LocalStorageService, SessionStorageService } from 'ng2-webstorage';
import { AuthExpiredInterceptor } from './auth-expired.interceptor';
import { ErrorHandlerInterceptor } from './errorhandler.interceptor';
import { NotificationInterceptor } from './notification.interceptor';

export function interceptableFactory(
    backend: XHRBackend,
    defaultOptions: RequestOptions,
    localStorage: LocalStorageService,
    sessionStorage: SessionStorageService,
    injector: Injector,
    JhiEventManager: JhiEventManager
) {
    return new JhiInterceptableHttp(
        backend,
        defaultOptions,
        [
            new AuthInterceptor(localStorage, sessionStorage),
            new AuthExpiredInterceptor(injector),
            // Other interceptors can be added here
            new ErrorHandlerInterceptor(JhiEventManager),
            new NotificationInterceptor()
        ]
    );
};

export function customHttpProvider() {
    return {
        provide: Http,
        useFactory: interceptableFactory,
        deps: [
            XHRBackend,
            RequestOptions,
            LocalStorageService,
            SessionStorageService,
            Injector,
            JhiEventManager
        ]
    };
};
