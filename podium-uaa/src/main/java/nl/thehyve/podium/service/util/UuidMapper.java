/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.service.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by bernd on 26/03/2017.
 */
@Component
public class UuidMapper {
    public String asString(UUID uuid) {
        return uuid.toString();
    }
}
