/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

export class Persona {
    public name: string;
    public properties: {[key: string]: any};

    constructor(name: string, properties: {[key: string]: any}) {
        this.name = name;
        this.properties = properties;
    }
}
