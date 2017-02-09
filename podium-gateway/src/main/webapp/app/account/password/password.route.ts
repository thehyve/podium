import { Route } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { PasswordComponent } from './password.component';

export const passwordRoute: Route = {
  path: 'password',
  component: PasswordComponent,
  data: {
      authorities: [
        'ROLE_PODIUM_ADMIN',
        'ROLE_BBMRI_ADMIN',
        'ROLE_ORGANISATION_ADMIN',
        'ROLE_ORGANISATION_COORDINATOR',
        'ROLE_REVIEWER',
        'ROLE_RESEARCHER'
      ],
      pageTitle: 'global.menu.account.password'
  },
  canActivate: [UserRouteAccessService]
};
