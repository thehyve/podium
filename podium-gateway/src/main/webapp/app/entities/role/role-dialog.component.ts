import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { Role } from './role.model';
import { RolePopupService } from './role-popup.service';
import { RoleService } from './role.service';
import { User, UserService } from '../../shared';
import { Organisation, OrganisationService } from '../organisation';
import { Authority } from '../../shared/authority/authority';
import { AUTHORITIES } from '../../shared/authority/authority.constants';

@Component({
    selector: 'jhi-role-dialog',
    templateUrl: './role-dialog.component.html'
})
export class RoleDialogComponent implements OnInit {

    role: Role;
    authorities: any[];
    isSaving: boolean;

    users: User[];
    organisations: Organisation[];
    authorityOptions: ReadonlyArray<Authority>;

    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private roleService: RoleService,
        private userService: UserService,
        private organisationService: OrganisationService,
        private eventManager: EventManager,
        private router: Router
    ) {
        this.jhiLanguageService.setLocations(['role']);
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_ORGANISATION_ADMIN'];
        this.authorityOptions = AUTHORITIES;
        this.userService.query().subscribe(
            (res: Response) => { this.users = res.json(); }, (res: Response) => this.onError(res.json()));
        this.organisationService.query().subscribe(
            (res: Response) => { this.organisations = res.json(); }, (res: Response) => this.onError(res.json()));
    }
    clear () {
        this.activeModal.dismiss('cancel');
        this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
    }

    save () {
        this.isSaving = true;
        if (this.role.id !== undefined) {
            this.roleService.update(this.role)
                .subscribe((res: Response) => this.onSaveSuccess(res), (res: Response) => this.onSaveError(res.json()));
        } else {
            this.onSaveError({message: 'Cannot save role without id.'});
        }
    }

    private onSaveSuccess (result) {
        this.eventManager.broadcast({ name: 'roleListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
        this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
    }

    private onSaveError (error) {
        this.isSaving = false;
        this.onError(error);
    }

    private onError (error) {
        this.alertService.error(error.message, null, null);
    }

    trackAuthorityByToken(index: number, item: string) {
        return item;
    }

    trackByUuid(index: number, item: any) {
        return item.uuid;
    }

    /**
     * @param selectedVals
     * @param option
     */
    getSelected(selectedVals: Array<string>, option: User): string {
        console.log(`selectedVals: ${selectedVals}, option: ${option}.`);
        if (selectedVals) {
            for (let i = 0; i < selectedVals.length; i++) {
                if (option.uuid === selectedVals[i]) {
                    return selectedVals[i];
                }
            }
        }
        return option.uuid;
    }
}

@Component({
    selector: 'jhi-role-popup',
    template: ''
})
export class RolePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor (
        private route: ActivatedRoute,
        private rolePopupService: RolePopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe(params => {
            if ( params['id'] ) {
                this.modalRef = this.rolePopupService
                    .open(RoleDialogComponent, params['id']);
            } else {
                this.modalRef = this.rolePopupService
                    .open(RoleDialogComponent);
            }

        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
