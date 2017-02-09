/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Directive, ElementRef, Input, Renderer, OnInit } from '@angular/core';
import { Principal } from './principal.service';

@Directive({
    selector: '[jhiHasAuthority]'
})
export class HasAuthorityDirective implements OnInit {

    @Input() jhiHasAuthority: string;
    authority: string;

    constructor(private principal: Principal, private el: ElementRef, private renderer: Renderer) {
    }

    ngOnInit() {
        this.authority = this.jhiHasAuthority.replace(/\s+/g, '');

        if (this.authority.length > 0) {
            this.setVisibilityAsync();
        }
        this.principal.getAuthenticationState().subscribe(identity => this.setVisibilitySync());
    }

    private setVisibilitySync() {
      if (this.principal.hasAnyAuthority([this.authority])) {
        this.setVisible();
      } else {
        this.setHidden();
      }
    }

    private setVisible () {
        this.renderer.setElementClass(this.el.nativeElement, 'hidden', false);
    }

    private setHidden () {
        this.renderer.setElementClass(this.el.nativeElement, 'hidden', true);
    }

    private setVisibilityAsync () {
        this.principal.hasAuthority(this.authority).then(result => {
            if (result) {
                this.setVisible();
            } else {
                this.setHidden();
            }
        });
    }
}
