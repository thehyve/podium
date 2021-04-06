/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { SpecialismComponent } from '../../../../../../main/webapp/app/shared/specialism/specialism.component';
import { FormsModule } from '@angular/forms';
import { PodiumTestModule } from '../../../test.module';

describe('SpecialismComponent (templateUrl)', () => {

    let comp: SpecialismComponent;
    let fixture: ComponentFixture<SpecialismComponent>;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                FormsModule,
                PodiumTestModule
            ],
            declarations: [SpecialismComponent], // declare the test component
        }).overrideTemplate(SpecialismComponent, '')
            .compileComponents();
    }));

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(SpecialismComponent);
        comp = fixture.componentInstance; // SpecialismComponent test instance
    });

    it('should not select anything when it is instantiated', () => {
        fixture.detectChanges();
        expect(comp.specialism).toBe(undefined);
    });

    it('should contain 20 options when it is instantiated', () => {
        fixture.detectChanges();
        expect(comp.specialismOptions.length).toEqual(20);
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
