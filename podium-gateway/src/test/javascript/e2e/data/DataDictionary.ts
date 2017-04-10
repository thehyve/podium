/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import SomeBank = require("./SomeBank")
import VarnameBank = require("./VarnameBank")
import XBank = require("./XBank")
import NewOrg = require("./NewOrg")
import Request01 = require("./Request01")

function initData () {
    let dataDictionary: {[key: string]: any} = Object.create(null);

    dataDictionary['SomeBank'] = SomeBank;
    dataDictionary['VarnameBank'] = VarnameBank;
    dataDictionary['XBank'] = XBank;
    dataDictionary['NewOrg'] = NewOrg;
    dataDictionary['Request01'] = Request01;
    dataDictionary['Request01'] = Request01;


    return dataDictionary;
}

export = initData;
