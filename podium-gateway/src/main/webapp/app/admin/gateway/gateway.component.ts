/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';

import { GatewayRoutesService } from './gateway-routes.service';
import { GatewayRoute } from './gateway-route.model';

@Component({
    selector: 'pdm-gateway',
    templateUrl: './gateway.component.html',
    providers: [ GatewayRoutesService ]
})
export class PdmGatewayComponent implements OnInit {

    gatewayRoutes: GatewayRoute[];
    updatingRoutes: Boolean;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private gatewayRoutesService: GatewayRoutesService
    ) {
        this.jhiLanguageService.setLocations(['gateway']);
    }

    ngOnInit () {
        this.refresh();
    }

    refresh () {
        this.updatingRoutes = true;
        this.gatewayRoutesService.findAll().subscribe(gatewayRoutes => {
            this.gatewayRoutes = gatewayRoutes;
            this.updatingRoutes = false;
        });
    }

}
