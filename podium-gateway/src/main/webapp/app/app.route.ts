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
import { NavbarComponent } from './layouts';
/* import { AuthService } from './shared';

@Injectable()
export class AuthorizeResolve implements Resolve<any> {

  constructor(private authService: AuthService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    return this.authService.authorize();
  }
} */

export const navbarRoute: Route = {
    path: '',
    component: NavbarComponent,
    // resolve: {
    //   'authorize': AuthorizeResolve
    // },
    outlet: 'navbar'
  };
