package org.bbmri.podium.repository.search;

import org.bbmri.podium.domain.Request;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Request entity.
 */
public interface RequestSearchRepository extends ElasticsearchRepository<Request, Long> {
}
