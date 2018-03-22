/*
 * Copyright (c) 2018. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Injectable } from '@angular/core';

@Injectable()
export class FormatHelper {

    static units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'];

    /**
     * Format bytes
     * @param bytes Number of bytes
     * @param precision Number of digits after the decimal point
     * @returns {string}
     */
    static formatBytes(bytes: number, precision: number): string {
        if (isNaN(bytes) || !isFinite(bytes)) {
            return '-';
        }
        if (typeof precision === 'undefined') {
            precision = 1;
        }
        const number = Math.min(Math.floor(Math.log(bytes) / Math.log(1000)), FormatHelper.units.length - 1);
        return (bytes / Math.pow(1000, Math.floor(number))).toFixed(precision) +  ' ' + FormatHelper.units[number];
    }

}
