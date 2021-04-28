/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Injectable } from '@angular/core';
import { SessionStorageService } from 'ngx-webstorage';

@Injectable({ providedIn: 'root' })
export class StateStorageService {
    private previousUrlKey = 'previousUrl';

    constructor(
        private $sessionStorage: SessionStorageService
    ) { }

    storeUrl(url: string): void {
        this.$sessionStorage.store(this.previousUrlKey, url);
    }

    getUrl(): string | null {
        return this.$sessionStorage.retrieve(this.previousUrlKey) as string | null;
    }

    clearUrl(): void {
        this.$sessionStorage.clear(this.previousUrlKey);
    }
}
