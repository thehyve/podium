/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({name: 'findLanguageFromKey'})
export class FindLanguageFromKeyPipe implements PipeTransform {
    private languages: any = {
        'ca': 'Català',
        'cs': 'Český',
        'da': 'Dansk',
        'de': 'Deutsch',
        'el': 'Ελληνικά',
        'en': 'English',
        'es': 'Español',
        'et': 'Eesti',
        'fr': 'Français',
        'gl': 'Galego',
        'hu': 'Magyar',
        'hi': 'हिंदी',
        'it': 'Italiano',
        'ja': '日本語',
        'ko': '한국어',
        'mr': 'मराठी',
        'nl': 'Nederlands',
        'pl': 'Polski',
        'pt-br': 'Português (Brasil)',
        'pt-pt': 'Português',
        'ro': 'Română',
        'ru': 'Русский',
        'sk': 'Slovenský',
        'sr': 'Srpski',
        'sv': 'Svenska',
        'ta': 'தமிழ்',
        'tr': 'Türkçe',
        'vi': 'Tiếng Việt',
        'zh-cn': '中文（简体）',
        'zh-tw': '繁體中文'
    };
    transform(lang: string): string {
        return this.languages[lang];
    }
}
