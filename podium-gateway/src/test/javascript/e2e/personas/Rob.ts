/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

class Rob {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "Rob";
        this.properties = {
            "userName": "admin",
            "password": "admin",
        }
    }
}

export = new Rob();
