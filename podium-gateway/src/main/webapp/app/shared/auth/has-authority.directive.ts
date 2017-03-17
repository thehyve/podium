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
    selector: '[pdmHasAuthority]'
})
export class HasAuthorityDirective {

    @Input() set pdmHasAuthority(value: string) {
        this.authority = value.replace(/\s+/g, '');

        if (this.authority.length > 0) {
            this.setVisibilityAsync();
        }
    };
    authority: string;

    constructor(
        private principal: Principal,
        private templateRef: TemplateRef<any>,
        private viewContainer: ViewContainerRef
    ) {
    }

    private setVisibilitySync() {
        if (this.principal.hasAnyAuthority([this.authority])) {
            this.setVisible();
        } else {
            this.setHidden();
        }
    }

    private setVisible () {
        this.viewContainer.createEmbeddedView(this.templateRef);
    }

    private setHidden () {
        this.viewContainer.clear();
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
