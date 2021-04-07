/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Route } from '@angular/router';
import { UserRouteAccessService } from '../../../core/auth/user-route-access.service';
import { PasswordResetInitComponent } from './password-reset-init.component';

export const passwordResetInitRoute: Route = {
  path: 'reset/request',
  component: PasswordResetInitComponent,
  data: {
    authorities: [],
    pageTitle: 'global.menu.account.password'
  },
  canActivate: [UserRouteAccessService]
};
