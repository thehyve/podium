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
import { Log } from './log.model';
import { LogsService } from './logs.service';

@Component({
    selector: 'pdm-logs',
    templateUrl: './logs.component.html',
})
export class LogsComponent implements OnInit {

    loggers: Log[];
    filter: string;
    orderProp: string;
    reverse: boolean;

    constructor (
        private logsService: LogsService
    ) {
        this.filter = '';
        this.orderProp = 'name';
        this.reverse = false;

    }

    ngOnInit() {
        this.logsService.findAll().subscribe(loggers => this.loggers = loggers);
    }

    changeLevel (name: string, level: string) {
        let log = new Log(name, level);
        this.logsService.changeLevel(log).subscribe(() => {
            this.logsService.findAll().subscribe(loggers => this.loggers = loggers);
        });
    }
}
