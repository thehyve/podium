/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

class VarnameBank {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "VarnameBank";
        this.properties = {
            "shortName": "VarnameBank",
            "name": "International variable name bank",
            "uuid": "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a",
            "activated": true
        }
    }
}

export = new VarnameBank();
