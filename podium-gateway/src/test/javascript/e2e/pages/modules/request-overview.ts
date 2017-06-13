/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import { $ } from 'protractor';
import { Interactable } from '../../protractor-stories/director';


function initLocators() {
    let elements: { [name: string]: Interactable };

    elements = {
        "HeaderTitle": { locator: $('th[jhisortby="requestDetail.title"]') },
        "HeaderStatus": { locator: $('th[jhisortby=status]') },
    };

    return elements;
}

export = initLocators;
