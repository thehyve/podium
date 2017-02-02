package org.bbmri.podium.repository.search;

import org.bbmri.podium.domain.Organisation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Organisation entity.
 */
public interface OrganisationSearchRepository extends ElasticsearchRepository<Organisation, Long> {
}
