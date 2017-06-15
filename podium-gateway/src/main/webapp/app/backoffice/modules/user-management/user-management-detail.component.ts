/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { User, UserService } from '../../../shared';

@Component({
    selector: 'pdm-user-mgmt-detail',
    templateUrl: './user-management-detail.component.html'
})
export class UserMgmtDetailComponent implements OnInit, OnDestroy {

    user: User;
    private subscription: any;

    constructor(
        private userService: UserService,
        private route: ActivatedRoute
    ) {

    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe(params => {
            this.load(params['login']);
        });
    }

    load (login) {
        this.userService.find(login).subscribe(user => {
            this.user = user;
        });
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

}
