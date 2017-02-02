import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { Organisation } from './organisation.model';
import { OrganisationService } from './organisation.service';

@Component({
    selector: 'jhi-organisation-detail',
    templateUrl: './organisation-detail.component.html'
})
export class OrganisationDetailComponent implements OnInit, OnDestroy {

    organisation: Organisation;
    private subscription: any;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private organisationService: OrganisationService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['organisation']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe(params => {
            this.load(params['id']);
        });
    }

    load (id) {
        this.organisationService.find(id).subscribe(organisation => {
            this.organisation = organisation;
        });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

}
