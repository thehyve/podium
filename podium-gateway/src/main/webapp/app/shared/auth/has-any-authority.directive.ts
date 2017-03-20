/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { Principal } from './principal.service';

@Directive({
    selector: '[pdmHasAnyAuthority]'
})
export class HasAnyAuthorityDirective {

    @Input() set pdmHasAnyAuthority(value: string) {
        this.authority = value.replace(/\s+/g, '').split(',');

        if (this.authority.length > 0) {
            this.setVisibilitySync();
        }
    };

    authority: string[];

    constructor(
        private principal: Principal,
        private templateRef: TemplateRef<any>,
        private viewContainer: ViewContainerRef
    ) {
    }

    private setVisible () {
        this.viewContainer.createEmbeddedView(this.templateRef);
    }

    private setHidden () {
        this.viewContainer.clear();
    }

    private setVisibilitySync () {
        let result = this.principal.hasAnyAuthority(this.authority);
        if (result) {
            this.setVisible();
        } else {
            this.setHidden();
        }
    }
}
