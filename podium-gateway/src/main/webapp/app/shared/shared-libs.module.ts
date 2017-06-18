/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule, Http } from '@angular/http';
import { CommonModule } from '@angular/common';
import { NgJhipsterModule } from 'ng-jhipster';
import { PodiumUploadModule } from './upload/upload.module';
import { TooltipModule } from 'ng2-bootstrap/tooltip';
import { TabsModule } from 'ng2-bootstrap/tabs';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { CookieModule } from 'ngx-cookie';
import { TranslateModule, TranslateLoader, TranslateService } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { UiSwitchModule } from 'angular2-ui-switch';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';

export function HttpLoaderFactory(http: Http) {
    return new TranslateHttpLoader(http, 'i18n/', '.json');
}

@NgModule({
    imports: [
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [Http]
            },
        }),
        NgbModule.forRoot(),
        NgJhipsterModule.forRoot({
            i18nEnabled: true,
            defaultI18nLang: 'en'
        }),
        InfiniteScrollModule,
        TooltipModule.forRoot(),
        TabsModule.forRoot(),
        UiSwitchModule,
        BsDropdownModule.forRoot(),
        CookieModule.forRoot()
    ],
    exports: [
        NgbModule,
        FormsModule,
        HttpModule,
        CommonModule,
        NgJhipsterModule,
        PodiumUploadModule,
        InfiniteScrollModule,
        TooltipModule,
        TabsModule,
        UiSwitchModule,
        BsDropdownModule
    ]
})
export class PodiumGatewaySharedLibsModule {
    constructor(private translate: TranslateService) {
        translate.addLangs(['en']);
        translate.setDefaultLang('en');

        let browserLang = translate.getBrowserLang();
        translate.use(browserLang.match(/en/) ? browserLang : 'en');
    }
}
