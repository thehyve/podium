/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

class NewOrg {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "NewOrg";
        this.properties = {
            "shortName": "NewOrg",
            "name": "New Organization",
            "activated": false
        }
    }
}

export = new NewOrg();
