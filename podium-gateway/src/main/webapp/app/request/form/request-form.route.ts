/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Route, Routes } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { RequestFormComponent } from './request-form.component';
import { RequestFormSubmitPopupComponent } from './request-form-submit-dialog.component';

export const requestFormSubmitRoute: Route = {
    path: 'uuid/:uuid',
    component: RequestFormSubmitPopupComponent,
    outlet: 'submit'
};

export const requestFormRoute: Route = {
  path: 'new',
  children: [requestFormSubmitRoute],
  component: RequestFormComponent,
  data: {
    authorities: ['ROLE_RESEARCHER'],
    pageTitle: 'request.pageTitle'
  },
  canActivate: [UserRouteAccessService]
};
