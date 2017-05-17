/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;


import nl.thehyve.podium.common.domain.AbstractPodiumEvent;

import javax.persistence.*;

/**
 * Application events to be stored in the database. E.g., status updates.
 */
@Entity
@Table(name = "podium_event")
public class PodiumEvent extends AbstractPodiumEvent {

}
