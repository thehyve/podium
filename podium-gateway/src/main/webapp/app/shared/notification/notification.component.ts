/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'pdm-notification',
    templateUrl: 'notification.component.html',
    styleUrls: ['notification.component.scss']
})

export class PodiumNotificationComponent implements OnInit {
    @Input()
    title?: string;
    description?: string;
    icon?: string;

    constructor() {}

    ngOnInit() {
        console.log('   asdsadasd');
        this.icon = 'warning';
    }
}
