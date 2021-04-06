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
import { AccountService } from '../../core/auth/account.service';

@Directive({
    selector: '[pdmHasAnyAuthority]'
})
export class HasAnyAuthorityDirective {
    private authorities: string[];

    constructor(
        private accountService: AccountService,
        private templateRef: TemplateRef<any>,
        private viewContainerRef: ViewContainerRef
    ) {}

    @Input() set pdmHasAnyAuthority(value: string|string[]) {
        this.authorities = typeof value === 'string' ? [ <string> value ] : <string[]> value;
        this.updateView();
        // Get notified each time authentication state changes.
        this.accountService.getAuthenticationState().subscribe(identity => this.updateView());
    }

    private updateView(): void {
        let hasAnyAuthority = this.accountService.hasAnyAuthority(this.authorities);
        this.viewContainerRef.clear();
        if (hasAnyAuthority) {
            this.viewContainerRef.createEmbeddedView(this.templateRef);
        }
    }
}
