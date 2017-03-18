/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var core_1 = require("@angular/core");
var PdmHealthService = (function () {
    function PdmHealthService(http) {
        this.http = http;
        this.separator = '.';
    }
    PdmHealthService.prototype.checkHealth = function () {
        return this.http.get('podiumuaa/management/health').map(function (res) { return res.json(); });
    };
    PdmHealthService.prototype.transformHealthData = function (data) {
        var response = [];
        this.flattenHealthData(response, null, data);
        return response;
    };
    PdmHealthService.prototype.getBaseName = function (name) {
        if (name) {
            var split = name.split('.');
            return split[0];
        }
    };
    PdmHealthService.prototype.getSubSystemName = function (name) {
        if (name) {
            var split = name.split('.');
            split.splice(0, 1);
            var remainder = split.join('.');
            return remainder ? ' - ' + remainder : '';
        }
    };
    /* private methods */
    PdmHealthService.prototype.addHealthObject = function (result, isLeaf, healthObject, name) {
        var status;
        var error;
        var healthData = {
            'name': name,
            'error': error,
            'status': status
        };
        var details = {};
        var hasDetails = false;
        for (var key in healthObject) {
            if (healthObject.hasOwnProperty(key)) {
                var value = healthObject[key];
                if (key === 'status' || key === 'error') {
                    healthData[key] = value;
                }
                else {
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
    };
    PdmHealthService.prototype.flattenHealthData = function (result, path, data) {
        for (var key in data) {
            if (data.hasOwnProperty(key)) {
                var value = data[key];
                if (this.isHealthObject(value)) {
                    if (this.hasSubSystem(value)) {
                        this.addHealthObject(result, false, value, this.getModuleName(path, key));
                        this.flattenHealthData(result, this.getModuleName(path, key), value);
                    }
                    else {
                        this.addHealthObject(result, true, value, this.getModuleName(path, key));
                    }
                }
            }
        }
        return result;
    };
    PdmHealthService.prototype.getModuleName = function (path, name) {
        var result;
        if (path && name) {
            result = path + this.separator + name;
        }
        else if (path) {
            result = path;
        }
        else if (name) {
            result = name;
        }
        else {
            result = '';
        }
        return result;
    };
    PdmHealthService.prototype.hasSubSystem = function (healthObject) {
        var result = false;
        for (var key in healthObject) {
            if (healthObject.hasOwnProperty(key)) {
                var value = healthObject[key];
                if (value && value.status) {
                    result = true;
                }
            }
        }
        return result;
    };
    PdmHealthService.prototype.isHealthObject = function (healthObject) {
        var result = false;
        for (var key in healthObject) {
            if (healthObject.hasOwnProperty(key)) {
                if (key === 'status') {
                    result = true;
                }
            }
        }
        return result;
    };
    return PdmHealthService;
}());
PdmHealthService = __decorate([
    core_1.Injectable()
], PdmHealthService);
exports.PdmHealthService = PdmHealthService;
