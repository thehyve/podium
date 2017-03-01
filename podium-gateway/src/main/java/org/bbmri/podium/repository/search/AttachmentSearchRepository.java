package org.bbmri.podium.repository.search;

import org.bbmri.podium.domain.Attachment;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Attachment entity.
 */
public interface AttachmentSearchRepository extends ElasticsearchRepository<Attachment, Long> {
}
