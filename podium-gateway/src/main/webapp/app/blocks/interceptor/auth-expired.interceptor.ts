/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { JhiHttpInterceptor } from 'ng-jhipster';
import { RequestOptionsArgs, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Injector } from '@angular/core';
import { AuthService } from '../../shared/auth/auth.service';
import { Principal } from '../../shared/auth/principal.service';

export class AuthExpiredInterceptor extends JhiHttpInterceptor {

    constructor(private injector: Injector) {
        super();
    }

    requestIntercept(options?: RequestOptionsArgs): RequestOptionsArgs {
        return options;
    }

    responseIntercept(observable: Observable<Response>): Observable<Response> {
        let self = this;

        return <Observable<Response>> observable.catch((error, source) => {
            if (error.status === 401) {
                let principal: Principal = self.injector.get(Principal);

                if (principal.isAuthenticated()) {
                    let auth: AuthService = self.injector.get(AuthService);
                    auth.authorize(true);
                }
            }
            return Observable.throw(error);
        });
    }
}
