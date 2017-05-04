/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.client;

import nl.thehyve.podium.common.resource.InternalUserResource;

@AuthorizedFeignClient(name = "podiumuaa")
public interface InternalUserClient extends InternalUserResource {
}
