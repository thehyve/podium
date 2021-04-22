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
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';
import { Observable } from 'rxjs';
import { first, map } from 'rxjs/operators';
import { RequestService } from '../../shared/request/request.service';
import { RequestBase } from '../../shared/request/request-base';
import { RequestDetail } from '../../shared/request/request-detail';

@Injectable()
export class RequestDetailResolver implements Resolve<RequestBase> {
    constructor(private requestService: RequestService) {}

    resolve (route: ActivatedRouteSnapshot): Observable<RequestDetail> {
        let uuid = route.params['uuid'];
        return this.requestService.findByUuid(uuid)
            .pipe(map(requestDetail => {
                if (requestDetail) {
                    return requestDetail;
                } else {
                    return null;
                }
            }), first());
    }
}
