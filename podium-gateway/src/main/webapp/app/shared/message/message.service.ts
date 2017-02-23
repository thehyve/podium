/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Message } from './message.model';
import { Injectable } from '@angular/core';

@Injectable()
export class MessageService {

    private message: Message;

    constructor() {
    }

    store(message: Message) {
        this.message = message;
    }

    get() {
        return this.message;
    }
}
