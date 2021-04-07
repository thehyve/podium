/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, ElementRef, Input, Renderer2 } from '@angular/core';

@Component({
    selector: 'pdm-password-strength-bar',
    template: `
        <div id="strength">
            <small jhiTranslate="global.messages.validate.newpassword.strength">Password strength:</small>
            <ul id="strengthBar">
                <li class="point"></li>
                <li class="point"></li>
                <li class="point"></li>
                <li class="point"></li>
                <li class="point"></li>
            </ul>
        </div>`,
    styleUrls: [
        'password-strength-bar.scss'
    ]
})
export class PasswordStrengthBarComponent {

    colors = ['#F00', '#F90', '#FF0', '#9F0', '#0F0'];

    constructor(private renderer: Renderer2, private elementRef: ElementRef) { }

    measureStrength(p: string): number {

        let force = 0;
        let regex = /[$-/:-?{-~!"^_`\[\]]/g; // "

        let lowerLetters = /[a-z]+/.test(p);
        let upperLetters = /[A-Z]+/.test(p);
        let numbers = /[0-9]+/.test(p);
        let symbols = regex.test(p);

        let flags = [lowerLetters, upperLetters, numbers, symbols];
        let passedMatches = flags.filter( (isMatchedFlag: boolean) => {
            return isMatchedFlag === true;
        }).length;

        force += 2 * p.length + ((p.length >= 10) ? 1 : 0);
        force += passedMatches * 10;

        // penality (short password)
        force = (p.length <= 6) ? Math.min(force, 10) : force;

        // penality (poor variety of characters)
        force = (passedMatches === 1) ? Math.min(force, 10) : force;
        force = (passedMatches === 2) ? Math.min(force, 20) : force;
        force = (passedMatches === 3) ? Math.min(force, 40) : force;

        return force;
    };

    getColor(s: number): any {
        let idx = 0;
        if (s <= 10) {
            idx = 0;
        } else if (s <= 20) {
            idx = 1;
        } else if (s <= 30) {
            idx = 2;
        } else if (s <= 40) {
            idx = 3;
        } else {
            idx = 4;
        }
        return {idx: idx + 1, col: this.colors[idx]};
    };

    @Input()
    set passwordToCheck(password: string) {
        if (password) {
            let c = this.getColor(this.measureStrength(password));
            let element = this.elementRef.nativeElement;
            if ( element.className ) {
                this.renderer.removeClass(element, element.className);
            }
            let lis = element.getElementsByTagName('li');
            for (let i = 0; i < lis.length; i++) {
                if (i < c.idx) {
                    this.renderer.setStyle(lis[i], 'backgroundColor', c.col);
                } else {
                    this.renderer.setStyle(lis[i], 'backgroundColor', '#DDD');
                }
            }
        }
    }
}
