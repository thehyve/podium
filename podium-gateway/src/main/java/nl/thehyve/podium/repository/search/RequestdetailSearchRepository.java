/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository.search;

import nl.thehyve.podium.domain.RequestDetail;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the RequestDetail entity.
 */
public interface RequestdetailSearchRepository extends ElasticsearchRepository<RequestDetail, Long> {
}
