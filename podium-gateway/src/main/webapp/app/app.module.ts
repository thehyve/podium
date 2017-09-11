import './vendor.ts';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { Ng2Webstorage } from 'ng2-webstorage';
import { customHttpProvider } from './blocks/interceptor/http.provider';
import { PaginationConfig } from './blocks/config/uib-pagination.config';
import { TypeaheadModule } from 'ng2-bootstrap';
import { PodiumGatewayRequestModule } from './request/request.module';
import { PodiumGatewayBbmriBackofficeModule } from './backoffice/bbmri/bbmri-backoffice.module';
import { PodiumGatewayOrganisationBackofficeModule } from './backoffice/organisation/organisation-backoffice.module';
import { PodiumGatewayPodiumBackofficeModule } from './backoffice/podium/podium-backoffice.module';
import { PodiumGatewaySharedModule, UserRouteAccessService } from './shared';
import { PodiumGatewayAdminModule } from './admin/admin.module';
import { PodiumGatewayAccountModule } from './account/account.module';
import { BreadcrumbsModule } from './shared/breadcrumbs/breadcrumbs.module';
import { OrganisationService } from './shared/organisation/organisation.service';
import { CommonModule } from '@angular/common';
import { RoleService } from './shared/role/role.service';
import { PdmMainComponent } from './main/main.component';
import { PageRibbonComponent } from './shared/profiles/page-ribbon.component';
import { AppRoutingModule } from './app.route';
import { HomeComponent } from './home/home.component';
import { FooterComponent } from './shared/footer/footer.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { NotFoundRoutingModule } from './shared/not-found/not-found.route';
import { RedirectService } from './shared/auth/redirect.service';

@NgModule({
    imports: [
        CommonModule,
        TypeaheadModule.forRoot(),
        BreadcrumbsModule.forRoot(),
        Ng2Webstorage.forRoot({ prefix: 'pdm', separator: '-'}),
        BrowserModule,
        AppRoutingModule,
        PodiumGatewaySharedModule,
        PodiumGatewayAdminModule,
        PodiumGatewayRequestModule,
        PodiumGatewayAccountModule,
        PodiumGatewayBbmriBackofficeModule,
        PodiumGatewayOrganisationBackofficeModule,
        PodiumGatewayPodiumBackofficeModule,
        NotFoundRoutingModule
    ],
    declarations: [
        DashboardComponent,
        PdmMainComponent,
        PageRibbonComponent,
        HomeComponent,
        FooterComponent
    ],
    providers: [
        { provide: Window, useValue: window },
        { provide: Document, useValue: document },
        customHttpProvider(),
        PaginationConfig,
        UserRouteAccessService,
        OrganisationService,
        RedirectService,
        RoleService
    ],
    bootstrap: [ PdmMainComponent ],
    exports: []
})
export class PodiumGatewayAppModule {}
