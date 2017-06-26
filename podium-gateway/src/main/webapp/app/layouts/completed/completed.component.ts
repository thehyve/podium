/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import { Message } from '../../shared/message/message.model';
import { MessageService } from '../../shared/message/message.service';

@Component({
    templateUrl: './completed.component.html'
})
export class CompletedComponent implements OnInit {

    completedTitle: string;
    completedContent: string;

    constructor(
        private messageService: MessageService
    ) {}

    ngOnInit() {
        let _message: Message = this.messageService.get();
        this.completedTitle = _message.messageTitle;
        this.completedContent = _message.messageBody;
    }
}
