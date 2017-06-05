/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

export class Account {
    constructor(
        public activated: boolean,
        public authorities: string[],
        public email: string,
        public telephone: string,
        public institute: string,
        public department: string,
        public jobTitle: string,
        public specialism: string,
        public firstName: string,
        public langKey: string,
        public lastName: string,
        public login: string,
        public imageUrl: string) {
    }
}
