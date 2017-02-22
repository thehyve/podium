/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

export class Message {
    public messageTitle?: string;
    public messageBody?: string;
    constructor (
        messageTitle?: string,
        messageBody?: string
    ) {
        this.messageTitle = messageTitle ? messageTitle : null;
        this.messageBody = messageBody ? messageBody : null;
    }
}
