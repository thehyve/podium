/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit } from '@angular/core';
import { ProfileService } from './profile.service';
import { ProfileInfo } from './profile-info.model';

@Component({
    selector: 'pdm-page-ribbon',
    template: `
        <div class="ribbon" *ngIf="hasRibbon()">
            <a href="" jhiTranslate="global.ribbon.{{ribbonEnv}}">{{ribbonEnv}}</a>
        </div>
    `,
    styleUrls: [
        'page-ribbon.scss'
    ]
})
export class PageRibbonComponent implements OnInit {

    profileInfo: ProfileInfo;
    ribbonEnv: string;

    constructor(private profileService: ProfileService) {}

    ngOnInit() {
        this.profileService.getProfileInfo().subscribe(profileInfo => {
            this.profileInfo = profileInfo;
            this.ribbonEnv = profileInfo.ribbonEnv;
        });
    }

    hasRibbon(): boolean {
        return this.profileInfo != null && !this.profileInfo.inProduction;
    }
}
