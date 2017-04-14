/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

var browserstack = require('browserstack-local');

exports.config = {
    ignoreUncaughtExceptions: true,
    allScriptsTimeout: 20000,

    'seleniumAddress': 'http://hub-cloud.browserstack.com/wd/hub',

    'capabilities': {
        'browserstack.user': process.env.BROWSERSTACK_USER,
        'browserstack.key': process.env.BROWSERSTACK_KEY,
        'os': 'Windows',
        'os_version': '7',
        'browserName': 'chrome',
        'resolution': '1024x768',
        'browserstack.local': true
    },

    specs: [
        './e2e/features/*.feature'
    ],

    // capabilities: {
    //     'browserName': 'chrome',
    // },

    // directConnect: true,

    baseUrl: 'http://localhost:8080/',

    framework: 'custom',
    frameworkPath: require.resolve('protractor-cucumber-framework'),

    cucumberOpts: {
        compiler: "ts:ts-node/register",
        strict: true,
        require: ['./e2e/stepdefinitions/*.ts'],
    },

    onPrepare: function () {
        browser.driver.manage().window().setSize(1280, 1024);
    },

    useAllAngular2AppRoots: true
};
