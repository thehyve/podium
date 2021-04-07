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
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Rx';

@Injectable({ providedIn: 'root' })
export class PdmConfigurationService {

    constructor(private http: HttpClient) {
    }

    get(): Observable<any> {
        return this.http.get('management/configprops').map((propertiesObject) => {
            let properties: any[] = [];

            for (let key in propertiesObject) {
                if (propertiesObject.hasOwnProperty(key)) {
                    properties.push(propertiesObject[key]);
                }
            }

            return properties.sort((propertyA, propertyB) => {
                return (propertyA.prefix === propertyB.prefix) ? 0 :
                       (propertyA.prefix < propertyB.prefix) ? -1 : 1;
            });
        });
    }

    getEnv(): Observable<any> {
        return this.http.get('management/env').map((propertiesObject) => {
            let properties: any = {};

            for (let key in propertiesObject) {
                if (propertiesObject.hasOwnProperty(key)) {
                    let valsObject = propertiesObject[key];
                    let vals: any[] = [];

                    for (let valKey in valsObject) {
                        if (valsObject.hasOwnProperty(valKey)) {
                            vals.push({key: valKey, val: valsObject[valKey]});
                        }
                    }
                    properties[key] = vals;
                }
            }

            return properties;
        });
    }
}
