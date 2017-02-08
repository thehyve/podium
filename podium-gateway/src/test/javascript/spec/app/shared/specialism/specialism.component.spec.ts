import {ComponentFixture, TestBed, async} from '@angular/core/testing';

import {By}              from '@angular/platform-browser';
import {DebugElement}    from '@angular/core';

import {SpecialismComponent} from '../../../../../../main/webapp/app/shared/specialism/specialism.component';
import {FormsModule} from '@angular/forms';

describe('SpecialismComponent (templateUrl)', () => {

    let comp: SpecialismComponent;
    let fixture: ComponentFixture<SpecialismComponent>;
    let de: DebugElement;
    let el: HTMLElement;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [ FormsModule ],
            declarations: [SpecialismComponent], // declare the test component
        }).compileComponents(); // compile template and css;
    }));

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(SpecialismComponent);
        comp = fixture.componentInstance; // SpecialismComponent test instance

        // query for the title <input> by CSS element selector
        de = fixture.debugElement.query(By.css('select'));
        el = de.nativeElement;
    });


    it('should set specialism into selected specialism option when selected value has changed', () => {
        fixture.detectChanges();
        comp.selectedSpecialism = 'foo';
        comp.onChange();
        expect(comp.specialism).toBe('foo');
    });

    it('should set specialism into empty string when selected value is `Other`', () => {
        fixture.detectChanges();
        comp.selectedSpecialism = 'Other';
        comp.onChange();
        expect(comp.specialism).toBe('');
    });

    it('should emit selected value', () => {
        fixture.detectChanges();
        spyOn(comp.specialismChange, 'emit');
        comp.selectedSpecialism = 'foo';
        comp.onChange();
        expect(comp.specialismChange.emit).toHaveBeenCalledWith('foo');
    });
});
