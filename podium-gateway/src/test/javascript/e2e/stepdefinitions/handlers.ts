/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { browser } from 'protractor';
import { Promise } from 'es6-promise';
let { defineSupportCode } = require('cucumber');

defineSupportCode(function({registerHandler}) {
    let delay = process.env.BROWSERSTACK_DELAY ? process.env.BROWSERSTACK_DELAY : 0;

    registerHandler('BeforeStep', function (features): Promise<any> {
        return  browser.sleep(delay);
    });
});
