/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';

@Component({
    selector: 'podium-specialism',
    templateUrl: './specialism.component.html',
    styleUrls: ['./specialism.scss']
})

export class SpecialismComponent implements OnInit {

    specialismValue: string;
    specialismOptions: any;
    selectedSpecialism: string;

    @Output() specialismChange = new EventEmitter();

    @Input()
    get specialism() {
        return this.specialismValue;
    }

    set specialism(val) {
        this.specialismValue = val;
        this.specialismChange.emit(this.specialismValue);
    }

    onChange() {
        this.specialism = this.selectedSpecialism === 'Other' ? '' : this.selectedSpecialism;
        this.specialismChange.emit(this.specialism);
    }

    ngOnInit() {
        this.selectedSpecialism = this.specialism;
        this.specialismOptions = [
            { value: '', display: '-- Please select specialism --' },
            { value: 'Gastroenterology', display: 'Gastroenterology'},
            { value: 'Gynaecology', display: 'Gynaecology'},
            { value: 'Dermatology', display: 'Dermatology'},
            { value: 'Medical Oncology', display: 'Medical Oncology'},
            { value: 'Internal Medicine', display: 'Internal Medicine'},
            { value: 'Radiology', display: 'Radiology'},
            { value: 'Radiotherapy', display: 'Radiotherapy'},
            { value: 'Haematology', display: 'Haematology'},
            { value: 'Throat-nose-ear', display: 'Throat-nose-ear'},
            { value: 'Surgery', display: 'Surgery'},
            { value: 'Epidemiology', display: 'Epidemiology'},
            { value: 'Primary care', display: 'Primary care'},
            { value: 'Cardiology', display: 'Cardiology'},
            { value: 'Pathology', display: 'Pathology'},
            { value: 'Lung Disease', display: 'Lung Disease'},
            { value: 'Urology', display: 'Urology'},
            { value: 'Neurology', display: 'Neurology'},
            { value: 'Endocrinology', display: 'Endocrinology'},
            { value: 'Other', display: '-- Other --'}
        ];
    }

}
