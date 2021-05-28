import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { PdmConfigurationComponent } from './configuration.component';
import { PdmConfigurationService } from './configuration.service';
import {Bean, PropertySource} from "app/admin/configuration/configuration.model";

describe('Component Tests', () => {
  describe('ConfigurationComponent', () => {
    let comp: PdmConfigurationComponent;
    let fixture: ComponentFixture<PdmConfigurationComponent>;
    let service: PdmConfigurationService;

    beforeEach(
      waitForAsync(() => {
        TestBed.configureTestingModule({
          imports: [HttpClientTestingModule],
          declarations: [PdmConfigurationComponent],
          providers: [PdmConfigurationService],
        })
          .overrideTemplate(PdmConfigurationComponent, '')
          .compileComponents();
      })
    );

    beforeEach(() => {
      fixture = TestBed.createComponent(PdmConfigurationComponent);
      comp = fixture.componentInstance;
      service = TestBed.inject(PdmConfigurationService);
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN
        const beans: Bean[] = [
          {
            prefix: 'jhipster',
            properties: {
              clientApp: {
                name: 'jhipsterApp',
              },
            },
          },
        ];
        const propertySources: PropertySource[] = [
          {
            name: 'server.ports',
            properties: {
              'local.server.port': {
                value: '8080',
              },
            },
          },
        ];
        spyOn(service, 'get').and.returnValue(of(beans));
        spyOn(service, 'getEnv').and.returnValue(of(propertySources));

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(service.get).toHaveBeenCalled();
        expect(service.getEnv).toHaveBeenCalled();
        expect(comp.configuration).toEqual(beans);
        expect(comp.allConfiguration).toEqual(propertySources);
      });
    });
  });
});
