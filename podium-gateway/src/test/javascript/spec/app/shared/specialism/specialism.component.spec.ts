import {ComponentFixture, TestBed, async} from '@angular/core/testing';

import {By}              from '@angular/platform-browser';
import {DebugElement}    from '@angular/core';

import {SpecialismComponent} from '../../../../../../main/webapp/app/shared/specialism/specialism.component';

describe('SpecialismComponent (templateUrl)', () => {

    let comp: SpecialismComponent;
    let fixture: ComponentFixture<SpecialismComponent>;
    let de: DebugElement;
    let el: HTMLElement;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [SpecialismComponent], // declare the test component
        }).compileComponents();  // compile template and css;
    }));

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(SpecialismComponent);
        comp = fixture.componentInstance; // SpecialismComponent test instance

        // query for the title <h1> by CSS element selector
        // de = fixture.debugElement.query(By('h1'));
        // el = de.nativeElement;
    });

});
