import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PodiumGatewaySharedModule } from '../../shared/shared.module';

import { HealthComponent } from './health.component';
import { HealthModalComponent } from './modal/health-modal.component';
import { healthRoute } from './health.route';

@NgModule({
  imports: [PodiumGatewaySharedModule, RouterModule.forChild([healthRoute])],
  declarations: [HealthComponent, HealthModalComponent],
  entryComponents: [HealthModalComponent],
})
export class HealthModule {}
