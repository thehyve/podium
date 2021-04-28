import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PodiumGatewaySharedModule } from '../../shared/shared.module';

import { ConfigurationComponent } from './configuration.component';
import { configurationRoute } from './configuration.route';

@NgModule({
    imports: [PodiumGatewaySharedModule, RouterModule.forChild([configurationRoute])],
    declarations: [ConfigurationComponent],
})
export class ConfigurationModule { }
