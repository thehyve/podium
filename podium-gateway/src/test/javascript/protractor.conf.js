/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

var HtmlScreenshotReporter = require("protractor-jasmine2-screenshot-reporter");
var JasmineReporters = require('jasmine-reporters');

exports.config = {

    allScriptsTimeout: 20000,

    specs: [
        './e2e/features/*.feature',
    ],

    capabilities: {
        'browserName': 'chrome',
    },

    directConnect: true,

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
