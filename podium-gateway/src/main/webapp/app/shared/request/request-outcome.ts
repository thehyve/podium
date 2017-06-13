/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

export enum RequestOutcome {
    Delivered           = <any>'Delivered',
    Partially_Delivered = <any>'Partially_Delivered',
    Cancelled           = <any>'Cancelled',
    Rejected            = <any>'Rejected',
    Approved            = <any>'Approved',
    None                = <any>'None'
}
