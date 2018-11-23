import { SpyObject } from './spyobject';
import { TranslateService } from '@ngx-translate/core';

export class MockTranslateService extends SpyObject {

    constructor() {
        super(TranslateService);
    }

}
