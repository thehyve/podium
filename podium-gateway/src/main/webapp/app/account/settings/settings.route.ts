import { Route } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { SettingsComponent } from './settings.component';

export const settingsRoute: Route = {
  path: 'settings',
  component: SettingsComponent,
  data: {
      authorities: [
        'ROLE_PODIUM_ADMIN',
        'ROLE_BBMRI_ADMIN',
        'ROLE_ORGANISATION_ADMIN',
        'ROLE_ORGANISATION_COORDINATOR',
        'ROLE_REVIEWER',
        'ROLE_RESEARCHER'
      ],
      pageTitle: 'global.menu.account.settings'
  },
  canActivate: [UserRouteAccessService]
};
