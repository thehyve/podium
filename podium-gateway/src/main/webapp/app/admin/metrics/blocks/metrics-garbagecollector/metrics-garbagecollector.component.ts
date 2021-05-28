import { Component, Input } from '@angular/core';

import { GarbageCollector } from '../../metrics.model';

@Component({
  selector: 'pdm-metrics-garbagecollector',
  templateUrl: './metrics-garbagecollector.component.html',
})
export class MetricsGarbageCollectorComponent {
  /**
   * object containing garbage collector related metrics
   */
  @Input() garbageCollectorMetrics?: GarbageCollector;

  /**
   * boolean field saying if the metrics are in the process of being updated
   */
  @Input() updating?: boolean;
}
