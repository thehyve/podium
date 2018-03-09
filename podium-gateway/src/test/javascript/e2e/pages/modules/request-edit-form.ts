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
        "title": { locator: $('input[name=title]') },
        "background": { locator: $('textarea[name=background]') },
        "researchQuestion": { locator: $('textarea[name=researchQuestion]') },
        "combinedRequestYes": { locator: $('.test-combinedRequestYes') },
        "hypothesis": { locator: $('textarea[name=hypothesis]') },
        "methods": { locator: $('textarea[name=methods]') },
        "relatedRequestNumber": { locator: $('input[name=relatedRequestNumber]') },
        "name": { locator: $('input[name=piName]') },
        "email": { locator: $('input[name=piEmail]') },
        "jobTitle": { locator: $('input[name=piFunction]') },
        "affiliation": { locator: $('input[name=piAffiliation]') },
        "searchQuery": { locator: $('textarea[name=searchQuery]') },
        "Data": { locator: $('input[value=Data]') },
        "Images": { locator: $('input[value=Images]') },
        "Material": { locator: $('input[value=Material]') },
        "organisations": { locator: $('#organisations') },
        "reset": { locator: $('#reset-form-btn') },
        "save": { locator: $('#save-draft-btn') },
        "submit": { locator: $('#submit-draft-btn') },
        "submit-modal": { locator: $('.test-submit-btn') },
        "cancel-modal": { locator: $('.test-cancel-btn') },
        "uploadFile": { locator: $('input[multiple=multiple]') },
        "removeFile": { locator: $("button[title='Remove attachment']")}
    };

    return elements;
}

export = initLocators;
