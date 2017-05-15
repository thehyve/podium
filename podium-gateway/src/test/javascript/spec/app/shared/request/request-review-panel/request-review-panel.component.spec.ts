/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { RequestReviewPanelComponent } from '../../../../../../../main/webapp/app/shared/request/request-review-panel/request-review-panel.component';
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

describe('RequestReviewPanelComponent (templateUrl)', () => {

    let comp: RequestReviewPanelComponent;
    let fixture: ComponentFixture<RequestReviewPanelComponent>;
    let de: DebugElement;
    let el: HTMLElement;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [],
            imports: [FormsModule],
            declarations: [RequestReviewPanelComponent], // declare the test component
        }).overrideComponent(RequestReviewPanelComponent, {
            set: {
                template: ''
            }
        }).compileComponents();

    }));

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(RequestReviewPanelComponent);
        comp = fixture.componentInstance; // OrganisationSelectorComponent test instance

        // de = fixture.debugElement.query(By.css('h1'));
        // el = de.nativeElement;
    });

    describe('ngOnInit', () => {
        it('should get last review feedback on initialisation', () => {
            // TODO
            comp.reviewRounds = [{}];
            comp.ngOnInit();
        });
    });

});
