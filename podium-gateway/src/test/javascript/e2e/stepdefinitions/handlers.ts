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

let myHandlers = function () {
    let delay = process.env.BROWSERSTACK_DELAY ? process.env.BROWSERSTACK_DELAY : 0;

    this.registerHandler('BeforeStep', function (features, callback) {
        browser.sleep(delay).then(() => {
            callback();
        });
    });
};

module.exports = myHandlers;
