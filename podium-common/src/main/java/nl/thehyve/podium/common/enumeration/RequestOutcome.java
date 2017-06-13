/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.common.enumeration;

/**
 * The RequestOutcome enumeration.
 */
public enum RequestOutcome {
    Delivered,
    Partially_Delivered,
    Cancelled,
    Approved,
    Rejected,
    None
}
