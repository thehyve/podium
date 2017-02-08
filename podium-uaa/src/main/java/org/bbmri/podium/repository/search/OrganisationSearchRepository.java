package org.bbmri.podium.repository.search;

import org.bbmri.podium.search.SearchOrganisation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Organisation entity.
 */
public interface OrganisationSearchRepository extends ElasticsearchRepository<SearchOrganisation, Long> {
}
