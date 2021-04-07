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
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PdmHealthService {

    separator: string;

    constructor (private http: HttpClient) {
        this.separator = '.';
    }

    checkHealth(): Observable<any> {
        return this.http.get('podiumuaa/management/health');
    }

    transformHealthData(data): any {
        let response = [];
        this.flattenHealthData(response, null, data);
        return response;
    }

    getBaseName(name): string {
        if (name) {
            let split = name.split('.');
            return split[0];
        }
    }

    getSubSystemName(name): string {
        if (name) {
            let split = name.split('.');
            split.splice(0, 1);
            let remainder = split.join('.');
            return remainder ? ' - ' + remainder : '';
        }
    }

    /* private methods */
    private addHealthObject(result, isLeaf, healthObject, name): any {

        let status: any;
        let error: any;
        let healthData: any = {
            'name': name,
            'error': error,
            'status': status
        };

        let details = {};
        let hasDetails = false;

        for (let key in healthObject) {
            if (healthObject.hasOwnProperty(key)) {
                let value = healthObject[key];
                if (key === 'status' || key === 'error') {
                    healthData[key] = value;
                } else {
                    if (!this.isHealthObject(value)) {
                        details[key] = value;
                        hasDetails = true;
                    }
                }
            }
        }

        // Add the details
        if (hasDetails) {
            healthData.details = details;
        }

        // Only add nodes if they provide additional information
        if (isLeaf || hasDetails || healthData.error) {
            result.push(healthData);
        }
        return healthData;
    }

    private flattenHealthData (result, path, data): any {
        for (let key in data) {
            if (data.hasOwnProperty(key)) {
                let value = data[key];
                if (this.isHealthObject(value)) {
                    if (this.hasSubSystem(value)) {
                        this.addHealthObject(result, false, value, this.getModuleName(path, key));
                        this.flattenHealthData(result, this.getModuleName(path, key), value);
                    } else {
                        this.addHealthObject(result, true, value, this.getModuleName(path, key));
                    }
                }
            }
        }

        return result;
    }

    private getModuleName (path, name): string {
        let result;
        if (path && name) {
            result = path + this.separator + name;
        }  else if (path) {
            result = path;
        } else if (name) {
            result = name;
        } else {
            result = '';
        }
        return result;
    }

    private hasSubSystem (healthObject): boolean {
        let result = false;

        for (let key in healthObject) {
            if (healthObject.hasOwnProperty(key)) {
                let value = healthObject[key];
                if (value && value.status) {
                    result = true;
                }
            }
        }

        return result;
    }

    private isHealthObject (healthObject): boolean {
        let result = false;

        for (let key in healthObject) {
            if (healthObject.hasOwnProperty(key)) {
                if (key === 'status') {
                    result = true;
                }
            }
        }

        return result;
    }
}
