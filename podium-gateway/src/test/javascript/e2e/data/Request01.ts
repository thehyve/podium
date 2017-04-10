/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

class Request01 {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "Request01";
        this.properties = {
            "title": "Request01",
            "background": "background01",
            "research question": "research question01",
            "hypothesis": "hypothesis01",
            "methods": "methods01",
            "related request number": "",
            "piName": "piName01",
            "piEmail": "piEmail01@localhost",
            "piFunction": "piFunction01",
            "piAffiliation": "piAffiliation01",
            "searchQuery": "searchQuery01",
            "type Data": true,
            "type Images": true,
            "type Material": true,
        }
    }
}

export = new Request01();
