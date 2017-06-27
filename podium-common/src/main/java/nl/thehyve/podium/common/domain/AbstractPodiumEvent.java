/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.domain;


import lombok.Data;
import nl.thehyve.podium.common.event.EventType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Application events to be stored in the database. E.g., status updates.
 */
@MappedSuperclass
@Data
public abstract class AbstractPodiumEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "podium_event_seq_gen")
    @GenericGenerator(
        name = "podium_event_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "podium_event_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    @Column(name = "event_id")
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String principal;

    @Column(name = "event_date")
    private Date eventDate = new Date();
    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @ElementCollection
    @Lob
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "podium_event_data", joinColumns=@JoinColumn(name="event_id"))
    private Map<String, String> data = new HashMap<>();
}
