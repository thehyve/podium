"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var core_1 = require('@angular/core');
var health_modal_component_1 = require('./health-modal.component');
var JhiHealthCheckComponent = (function () {
    function JhiHealthCheckComponent(jhiLanguageService, modalService, healthService) {
        this.jhiLanguageService = jhiLanguageService;
        this.modalService = modalService;
        this.healthService = healthService;
        this.jhiLanguageService.setLocations(['health']);
    }
    JhiHealthCheckComponent.prototype.ngOnInit = function () {
        this.refresh();
    };
    JhiHealthCheckComponent.prototype.baseName = function (name) {
        return this.healthService.getBaseName(name);
    };
    JhiHealthCheckComponent.prototype.getTagClass = function (statusState) {
        if (statusState === 'UP') {
            return 'tag-success';
        }
        else {
            return 'tag-danger';
        }
    };
    JhiHealthCheckComponent.prototype.refresh = function () {
        var _this = this;
        this.updatingHealth = true;
        this.healthService.checkHealth().subscribe(function (health) {
            _this.healthData = _this.healthService.transformHealthData(health);
            _this.updatingHealth = false;
        });
    };
    JhiHealthCheckComponent.prototype.showHealth = function (health) {
        var modalRef = this.modalService.open(health_modal_component_1.JhiHealthModalComponent);
        modalRef.componentInstance.currentHealth = health;
        modalRef.result.then(function (result) {
            console.log("Closed with: " + result);
        }, function (reason) {
            console.log("Dismissed " + reason);
        });
    };
    JhiHealthCheckComponent.prototype.subSystemName = function (name) {
        return this.healthService.getSubSystemName(name);
    };
    JhiHealthCheckComponent = __decorate([
        core_1.Component({
            selector: 'jhi-health',
            templateUrl: './health.component.html',
        })
    ], JhiHealthCheckComponent);
    return JhiHealthCheckComponent;
}());
exports.JhiHealthCheckComponent = JhiHealthCheckComponent;
