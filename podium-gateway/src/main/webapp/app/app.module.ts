import './vendor.ts';
import { AuthInterceptor } from './blocks/interceptor/auth.interceptor';

import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { BrowserModule } from '@angular/platform-browser';
import { Ng2Webstorage } from 'ng2-webstorage';

import { PodiumGatewaySharedModule, UserRouteAccessService } from './shared';
import { PodiumGatewayAdminModule } from './admin/admin.module';
import { PodiumGatewayAccountModule } from './account/account.module';
import { PodiumGatewayEntityModule } from './entities/entity.module';

import { LayoutRoutingModule } from './layouts';
import { HomeComponent } from './home';
import { DashboardComponent } from './dashboard';

import { customHttpProvider } from './blocks/interceptor/http.provider';
import { PaginationConfig } from './blocks/config/uib-pagination.config';

import {
    JhiMainComponent,
    NavbarComponent,
    FooterComponent,
    ProfileService,
    PageRibbonComponent,
    ActiveMenuDirective,
    ErrorComponent
} from './layouts';

@NgModule({
    imports: [
        BrowserModule,
        LayoutRoutingModule,
        Ng2Webstorage.forRoot({ prefix: 'pdm', separator: '-'}),
        PodiumGatewaySharedModule,
        PodiumGatewayAdminModule,
        PodiumGatewayAccountModule,
        PodiumGatewayEntityModule
    ],
    declarations: [
        JhiMainComponent,
        HomeComponent,
        DashboardComponent,
        NavbarComponent,
        ErrorComponent,
        PageRibbonComponent,
        ActiveMenuDirective,
        FooterComponent
    ],
    providers: [
        ProfileService,
        { provide: Window, useValue: window },
        { provide: Document, useValue: document },
        customHttpProvider(),
        PaginationConfig,
        UserRouteAccessService
    ],
    bootstrap: [ JhiMainComponent ]
})
export class PodiumGatewayAppModule {}
