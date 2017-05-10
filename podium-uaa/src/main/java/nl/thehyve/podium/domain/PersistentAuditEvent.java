/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import nl.thehyve.podium.common.domain.AbstractPodiumEvent;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Audit events to be stored in the database.
 */
@Entity
@Table(name = "podium_event")
public class PersistentAuditEvent extends AbstractPodiumEvent {

}
