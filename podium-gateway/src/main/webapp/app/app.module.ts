import { NgModule, LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import locale from '@angular/common/locales/en';
import { BrowserModule, Title } from '@angular/platform-browser';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { TranslateModule, TranslateService, TranslateLoader, MissingTranslationHandler } from '@ngx-translate/core';
import { NgxWebstorageModule } from 'ngx-webstorage';
import { httpInterceptorProviders } from './core/interceptor/http.provider';
import { translatePartialLoader, missingTranslationHandler } from './config/translation.config';
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
import {
    AppRoutingModule,
    AppErrorRoutingModule
} from './app-routing.module';
import { PdmHomeComponent } from './home/home.component';
import { FooterComponent } from './layouts/footer/footer.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ActiveMenuDirective } from './layouts/navbar/active-menu.directive';

@NgModule({
    imports: [
        BrowserModule,
        HttpClientModule,
        CommonModule,
        TypeaheadModule.forRoot(),
        BreadcrumbsModule.forRoot(),
        NgxWebstorageModule.forRoot({ prefix: 'pdm', separator: '-' }),
        AppRoutingModule,
        PodiumGatewaySharedModule,
        PodiumGatewayAdminModule,
        PodiumGatewayRequestModule,
        PodiumGatewayAccountModule,
        PodiumGatewayBbmriBackofficeModule,
        PodiumGatewayOrganisationBackofficeModule,
        PodiumGatewayPodiumBackofficeModule,
        AppErrorRoutingModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: translatePartialLoader,
                deps: [HttpClient],
            },
            missingTranslationHandler: {
                provide: MissingTranslationHandler,
                useFactory: missingTranslationHandler,
            },
        }),
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
        Title,
        { provide: LOCALE_ID, useValue: 'en' },
        httpInterceptorProviders,
        UserRouteAccessService,
    ],
    bootstrap: [PdmMainComponent],
    exports: []
})
export class PodiumGatewayAppModule {
    constructor(
        translateService: TranslateService
    ) {
        registerLocaleData(locale);
        translateService.setDefaultLang('en');
        translateService.use('en');
    }
}
