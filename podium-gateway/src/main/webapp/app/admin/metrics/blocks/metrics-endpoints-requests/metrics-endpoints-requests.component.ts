import { Component, Input } from '@angular/core';

import { Services } from '../../metrics.model';

@Component({
  selector: 'pdm-metrics-endpoints-requests',
  templateUrl: './metrics-endpoints-requests.component.html',
})
export class MetricsEndpointsRequestsComponent {
  /**
   * object containing service related metrics
   */
  @Input() endpointsRequestsMetrics?: Services;

  /**
   * boolean field saying if the metrics are in the process of being updated
   */
  @Input() updating?: boolean;
}
