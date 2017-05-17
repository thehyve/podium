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
import { Http } from '@angular/http';
import { RequestBase } from '../../shared/request/request-base';

@Injectable()
export class RequestFormService {

    request: RequestBase;

    constructor(private http: Http) {

    }
}


