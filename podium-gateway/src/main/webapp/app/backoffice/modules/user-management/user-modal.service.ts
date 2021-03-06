/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Injectable, Component } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { User } from '../../../shared/user/user.model';
import { UserService } from '../../../shared/user/user.service';

@Injectable({ providedIn: 'root' })
export class UserModalService {
    private isOpen = false;
    constructor (
        private modalService: NgbModal,
        private userService: UserService
    ) {}

    open (component: any, login?: string): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (login) {
            this.userService.find(login).subscribe(user => this.userModalRef(component, user));
        } else {
            return this.userModalRef(component, new User());
        }
    }

    userModalRef(component: Component, user: User): NgbModalRef {
        let modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.user = user;
        modalRef.result.then(() => {
            this.isOpen = false;
        }, () => {
            this.isOpen = false;
        });
        return modalRef;
    }
}
