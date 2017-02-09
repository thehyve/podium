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

import { UserRouteAccessService } from '../../shared';
import { JhiDocsComponent } from './docs.component';

export const docsRoute: Route = {
  path: 'docs',
  component: JhiDocsComponent,
  data: {
    pageTitle: 'global.menu.admin.apidocs'
  }
};
