package org.bbmri.podium.repository.search;

import org.bbmri.podium.domain.RequestDetail;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the RequestDetail entity.
 */
public interface RequestdetailSearchRepository extends ElasticsearchRepository<RequestDetail, Long> {
}
