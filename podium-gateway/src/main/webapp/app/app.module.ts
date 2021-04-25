import './vendor.ts';
import { NgModule, LOCALE_ID } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { NgxWebstorageModule } from 'ngx-webstorage';
import { customHttpProvider } from './blocks/interceptor/http.provider';
import { TypeaheadModule } from 'ngx-bootstrap';
import { PodiumGatewayRequestModule } from './request/request.module';
import { PodiumGatewayBbmriBackofficeModule } from './backoffice/bbmri/bbmri-backoffice.module';
import { PodiumGatewayOrganisationBackofficeModule } from './backoffice/organisation/organisation-backoffice.module';
import { PodiumGatewayPodiumBackofficeModule } from './backoffice/podium/podium-backoffice.module';
import { PodiumGatewaySharedModule } from './shared/shared.module';
import { UserRouteAccessService } from './core/auth/user-route-access.service';
import { PodiumGatewayAdminModule } from './admin/admin.module';
import { PodiumGatewayAccountModule } from './account/account.module';
import { BreadcrumbsModule } from './shared/breadcrumbs/breadcrumbs.module';
import { CommonModule } from '@angular/common';
import { PdmMainComponent } from './layouts/main/main.component';
import { PageRibbonComponent } from './layouts/profiles/page-ribbon.component';
import { AppRoutingModule } from './app-routing.module';
import { PdmHomeComponent } from './home/home.component';
import { FooterComponent } from './layouts/footer/footer.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { NotFoundRoutingModule } from './shared/not-found/not-found.route';
import { ActiveMenuDirective } from './layouts/navbar/active-menu.directive';

@NgModule({
    imports: [
        BrowserModule,
        HttpClientModule,
        CommonModule,
        TypeaheadModule.forRoot(),
        BreadcrumbsModule.forRoot(),
        NgxWebstorageModule.forRoot({ prefix: 'pdm', separator: '-'}),
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
        PdmHomeComponent,
        FooterComponent,
        ActiveMenuDirective,
    ],
    providers: [
        { provide: Window, useValue: window },
        { provide: Document, useValue: document },
        { provide: LOCALE_ID, useValue: 'en' },
        customHttpProvider(),
        PaginationConfig,
        UserRouteAccessService,
    ],
    bootstrap: [ PdmMainComponent ],
    exports: []
})
export class PodiumGatewayAppModule {}
