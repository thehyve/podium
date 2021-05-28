/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Directive, OnInit, ElementRef, Renderer2, Input } from '@angular/core';
import { TranslateService, LangChangeEvent } from '@ngx-translate/core';

@Directive({
    selector: '[pdmActiveMenu]'
})
export class ActiveMenuDirective implements OnInit {
    @Input() pdmActiveMenu: string;

    constructor(private el: ElementRef, private renderer: Renderer2, private translateService: TranslateService) { }

    ngOnInit() {
        this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
            this.updateActiveFlag(event.lang);
        });
        this.updateActiveFlag(this.translateService.currentLang);
    }

    updateActiveFlag(selectedLanguage: string) {
        if (this.pdmActiveMenu === selectedLanguage) {
            this.renderer.addClass(this.el.nativeElement, 'active');
        } else {
            this.renderer.removeClass(this.el.nativeElement, 'active');
        }
    }
}
