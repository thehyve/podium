import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { PdmMetricsMonitoringComponent } from './metrics.component';
import { PdmMetricsService } from './metrics.service';

describe('Component Tests', () => {
  describe('MetricsComponent', () => {
    let comp: PdmMetricsMonitoringComponent;
    let fixture: ComponentFixture<PdmMetricsMonitoringComponent>;
    let service: PdmMetricsService;

    beforeEach(
      waitForAsync(() => {
        TestBed.configureTestingModule({
          imports: [HttpClientTestingModule],
          declarations: [PdmMetricsMonitoringComponent],
        })
          .overrideTemplate(PdmMetricsMonitoringComponent, '')
          .compileComponents();
      })
    );

    beforeEach(() => {
      fixture = TestBed.createComponent(PdmMetricsMonitoringComponent);
      comp = fixture.componentInstance;
      service = TestBed.inject(PdmMetricsService);
    });

    describe('refresh', () => {
      it('should call refresh on init', () => {
        // GIVEN
        const response = {
          timers: {
            service: 'test',
            unrelatedKey: 'test',
          },
          gauges: {
            'jcache.statistics': {
              value: 2,
            },
            unrelatedKey: 'test',
          },
        };
        spyOn(service, 'getMetrics').and.returnValue(of(response));

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(service.getMetrics).toHaveBeenCalled();
      });
    });
  });
});
