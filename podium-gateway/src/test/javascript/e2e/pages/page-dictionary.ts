/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { Page } from '../protractor-stories/director';
import SigninPage = require("./signin.page");
import DashboardPage = require("./dashboard.page");
import ProfilePage = require("./profile.page");
import RegistrationPage = require("./registration.page");
import UserManagementPage = require("./user-management.page");
import UserDetailsPage = require("./user-details.page");
import OrganisationsManagementPage = require("./organisations-management.page");
import OrganisationsConfigurationPage = require("./organisations-configuration.page");
import OrganisationsDetailsPage = require("./organisations-details.page");
import OrganisationsEditPage = require("./organisations-edit.page");
import CreateOrganisationPage = require("./create-organisation.page");
import NewRequestsPage = require("./new-requests.page");
import CompletedPage = require('./completed.page');
import RequestsManagementPage = require('./requests-management.page');
import ReviseRequestsPage = require('./revise-requests.page');
import RequestDetailsPage = require('./requests-details.page');
import EditRequestsPage = require('./edit-requests.page');
import OrganisationRequestsManagementPage = require('./requests-management-org.page');


/*
 * pages must be added to this dictionary for the director class to find them.
 */

function initPages() {
    let PageDictionary: { [key: string]: Page } = Object.create(null);

    PageDictionary['sign in'] = new SigninPage;
    PageDictionary['Dashboard'] = new DashboardPage;
    PageDictionary['profile'] = new ProfilePage;
    PageDictionary['registration'] = new RegistrationPage;
    PageDictionary['completed'] = new CompletedPage;
    PageDictionary['user management'] = new UserManagementPage;
    PageDictionary['user details'] = new UserDetailsPage;
    PageDictionary['organisation overview'] = new OrganisationsManagementPage();
    PageDictionary['organisation configuration'] = new OrganisationsConfigurationPage();
    PageDictionary['organisation details'] = new OrganisationsDetailsPage();
    PageDictionary['organisation edit'] = new OrganisationsEditPage();
    PageDictionary['create organisation'] = new CreateOrganisationPage();
    PageDictionary['new requests'] = new NewRequestsPage();
    PageDictionary['request overview'] = new RequestsManagementPage();
    PageDictionary['request details'] = new RequestDetailsPage();
    PageDictionary['revise requests'] = new ReviseRequestsPage();
    PageDictionary['edit requests'] = new EditRequestsPage();
    PageDictionary['organisation request overview'] = new OrganisationRequestsManagementPage();

    return PageDictionary;
}

export = initPages;
