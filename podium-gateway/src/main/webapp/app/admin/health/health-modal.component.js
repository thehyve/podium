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
var PdmHealthModalComponent = (function () {
    function PdmHealthModalComponent(healthService, activeModal) {
        this.healthService = healthService;
        this.activeModal = activeModal;
    }
    PdmHealthModalComponent.prototype.baseName = function (name) {
        return this.healthService.getBaseName(name);
    };
    PdmHealthModalComponent.prototype.subSystemName = function (name) {
        return this.healthService.getSubSystemName(name);
    };
    PdmHealthModalComponent.prototype.readableValue = function (value) {
        if (this.currentHealth.name !== 'diskSpace') {
            return value.toString();
        }
        // Should display storage space in an human readable unit
        var val = value / 1073741824;
        if (val > 1) {
            return val.toFixed(2) + ' GB';
        }
        else {
            return (value / 1048576).toFixed(2) + ' MB';
        }
    };
    return PdmHealthModalComponent;
}());
PdmHealthModalComponent = __decorate([
    core_1.Component({
        selector: 'pdm-health-modal',
        templateUrl: './health-modal.component.html'
    })
], PdmHealthModalComponent);
exports.PdmHealthModalComponent = PdmHealthModalComponent;
