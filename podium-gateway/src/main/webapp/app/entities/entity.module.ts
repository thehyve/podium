import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { PodiumGatewayRoleModule } from './role/role.module';
import { PodiumGatewayOrganisationModule } from './organisation/organisation.module';
/* jhipster-needle-add-entity-module-import - JHipster will add entity modules imports here */

@NgModule({
    imports: [
        PodiumGatewayRoleModule,
        PodiumGatewayOrganisationModule,
        /* jhipster-needle-add-entity-module - JHipster will add entity modules here */
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayEntityModule {}
