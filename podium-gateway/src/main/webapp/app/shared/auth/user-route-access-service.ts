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
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../';
import { StateStorageService } from './state-storage.service';

@Injectable()
export class UserRouteAccessService implements CanActivate {

    constructor(
        private router: Router,
        private auth: AuthService,
        private stateStorageService: StateStorageService) {}

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | Promise<boolean> {
        this.setStateStorage(route, state);
        return this.auth.authorize(false).then( canActivate => {
            return canActivate;
        });
    }

    canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | Promise<boolean> {
        return this.canActivate(route, state);
    }

    setStateStorage(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        let params = {};
        let destinationData = {};
        let destinationName = '';
        let destinationEvent = route;
        if (destinationEvent !== undefined) {
            params = destinationEvent.params;
            destinationData = destinationEvent.data;
            destinationName = state.url;
        }
        let from = {name: this.router.url.slice(1)};
        let destination = {name: destinationName, data: destinationData};
        this.stateStorageService.storeDestinationState(destination, params, from);
    }
}
