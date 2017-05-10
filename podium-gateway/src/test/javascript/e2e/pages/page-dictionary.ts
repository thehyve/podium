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
import SigninPage = require("./signin.page")
import DashboardPage = require("./dashboard.page")
import ProfilePage = require("./profile.page")
import RegistrationPage = require("./registration.page")
import completedPage = require("./completed.page")
import UserManagementPage = require("./user-management.page")
import UserDetailsPage = require("./user-details.page")
import OrganisationsManagementPage = require("./organisations-management.page")
import OrganisationsDetailsPage = require("./organisations-details.page")
import CreateOrganisationPage = require("./create-organisation.page")
import NewRequestsPage = require("./new-requests.page")




/*
 * pages must be added to this dictionary for the director class to find them.
 */

function initPages () {
    let PageDictionary: {[key: string]: Page} = Object.create(null);

    PageDictionary['sign in'] = new SigninPage;
    PageDictionary['Dashboard'] = new DashboardPage;
    PageDictionary['profile'] = new ProfilePage;
    PageDictionary['registration'] = new RegistrationPage;
    PageDictionary['completed'] = new completedPage;
    PageDictionary['user management'] = new UserManagementPage;
    PageDictionary['user details'] = new UserDetailsPage;
    PageDictionary['organization overview'] = new OrganisationsManagementPage();
    PageDictionary['organization details'] = new OrganisationsDetailsPage();
    PageDictionary['create organisation'] = new CreateOrganisationPage();
    PageDictionary['new requests'] = new NewRequestsPage();

    return PageDictionary;
}

export = initPages;
