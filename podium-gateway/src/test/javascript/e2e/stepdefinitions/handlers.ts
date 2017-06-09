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
import { AdminConsole } from '../protractor-stories/admin-console';
let { defineSupportCode } = require('cucumber');

defineSupportCode(function ({ registerHandler }) {
    let delay = process.env.BROWSERSTACK_DELAY ? process.env.BROWSERSTACK_DELAY : 0;

    registerHandler('BeforeStep', function (step): Promise<any> {
        return browser.sleep(delay);
    });

    registerHandler('BeforeFeatures', function (features) {
        let adminConsole = new AdminConsole();

        return adminConsole.authenticate().then(() => {
        }, () => {
            browser.driver.quit(); //clean up the driver instance
            return Promise.reject("\n\n" +
                "   =================================================\n" +
                "   !!COULD NOT LOG INTO PODIUM. SKIPPING ALL TESTS!!\n" +
                "   =================================================\n");
        })
    });
});
