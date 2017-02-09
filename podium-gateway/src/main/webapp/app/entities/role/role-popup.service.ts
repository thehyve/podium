import { Injectable, Component } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Role } from './role.model';
import { RoleService } from './role.service';
@Injectable()
export class RolePopupService {
    private isOpen = false;
    constructor (
        private modalService: NgbModal,
        private roleService: RoleService
    ) {}

    open (component: Component, id?: number | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            this.roleService.find(id).subscribe(role => {
                this.roleModalRef(component, role);
            });
        } else {
            return this.roleModalRef(component, new Role());
        }
    }

    roleModalRef(component: Component, role: Role): NgbModalRef {
        let modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.role = role;
        modalRef.result.then(result => {
            console.log(`Closed with: ${result}`);
            this.isOpen = false;
        }, (reason) => {
            console.log(`Dismissed ${reason}`);
            this.isOpen = false;
        });
        return modalRef;
    }
}
