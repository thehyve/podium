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
import { Message } from '../message/message.model';
import { MessageService } from '../message/message.service';
import { Router } from '@angular/router';
import { CompletionType } from './completion-type';

@Component({
    templateUrl: './completed.component.html',
    styleUrls: ['./completed.scss']
})
export class CompletedComponent implements OnInit {

    completedTitle: string;
    completedContent: string;
    type: CompletionType;
    completionTypes: typeof CompletionType = CompletionType;

    constructor(
        private messageService: MessageService,
        private router: Router
    ) {}

    ngOnInit() {
        let _message: Message = this.messageService.get();

        if (_message !== undefined) {
            this.completedTitle = _message.messageTitle;
            this.completedContent = _message.messageBody;
            this.type = _message.type;
        } else {
            this.router.navigate(['/']);
        }
    }

    isCompletionType(type: CompletionType) {
        return this.type === type;
    }
}
