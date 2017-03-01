/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import {Page} from "../protractor-stories/director";
import SigninPage = require("./SigninPage")
import DashboardPage = require("./DashboardPage")
/*
 * pages must be added to this dictionary for the director class to find them.
 */
let PageDictionary: {[key: string]: Page} = Object.create(null);

PageDictionary['SigninPage'] = new SigninPage;
PageDictionary['DashboardPage'] = new DashboardPage;

export = PageDictionary;
