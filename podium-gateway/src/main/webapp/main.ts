/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { DEBUG_INFO_ENABLED } from './app/app.constants';
import { PodiumGatewayAppModule } from './app/app.module';

// disable debug data on prod profile to improve performance
if (!DEBUG_INFO_ENABLED) {
    enableProdMode();
}

platformBrowserDynamic()
    .bootstrapModule(PodiumGatewayAppModule, { preserveWhitespaces: true })
    .then(() => console.log('Application started'))
    .catch(err => console.error(err));
