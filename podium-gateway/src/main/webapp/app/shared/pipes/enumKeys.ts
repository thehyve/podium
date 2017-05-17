/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({name: 'enumKeys'})
export class EnumKeysPipe implements PipeTransform {
    transform(value, args: string[]): any {
        let keys = [];
        for (let enumMember in value) {
            if (value.hasOwnProperty(enumMember)) {
                let isValueProperty = parseInt(enumMember, 10) >= 0;
                if (isValueProperty) {
                    keys.push({key: enumMember, value: value[enumMember]});
                }
            }
        }
        return keys;
    }
}

