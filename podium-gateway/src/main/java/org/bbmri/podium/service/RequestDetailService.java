package org.bbmri.podium.service;

import org.bbmri.podium.domain.RequestDetail;
import org.bbmri.podium.repository.RequestDetailRepository;
import org.bbmri.podium.repository.search.RequestdetailSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing RequestDetail.
 */
@Service
@Transactional
public class RequestDetailService {

    private final Logger log = LoggerFactory.getLogger(RequestDetailService.class);

    private final RequestDetailRepository requestDetailRepository;

    private final RequestdetailSearchRepository requestdetailSearchRepository;

    public RequestDetailService(RequestDetailRepository requestDetailRepository, RequestdetailSearchRepository requestdetailSearchRepository) {
        this.requestDetailRepository = requestDetailRepository;
        this.requestdetailSearchRepository = requestdetailSearchRepository;
    }

    /**
     * Save a requestDetail.
     *
     * @param requestDetail the entity to save
     * @return the persisted entity
     */
    public RequestDetail save(RequestDetail requestDetail) {
        log.debug("Request to save RequestDetail : {}", requestDetail);
        RequestDetail result = requestDetailRepository.save(requestDetail);
        requestdetailSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the requestdetails.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestDetail> findAll(Pageable pageable) {
        log.debug("Request to get all Requestdetails");
        Page<RequestDetail> result = requestDetailRepository.findAll(pageable);
        return result;
    }

    /**
     *  get all the requestdetails where Request is null.
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<RequestDetail> findAllWhereRequestIsNull() {
        log.debug("Request to get all requestdetails where Request is null");
        return StreamSupport
            .stream(requestDetailRepository.findAll().spliterator(), false)
            .filter(requestdetail -> requestdetail.getRequest() == null)
            .collect(Collectors.toList());
    }

    /**
     *  Get one requestdetail by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public RequestDetail findOne(Long id) {
        log.debug("Request to get RequestDetail : {}", id);
        RequestDetail requestDetail = requestDetailRepository.findOne(id);
        return requestDetail;
    }

    /**
     *  Delete the  requestdetail by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete RequestDetail : {}", id);
        requestDetailRepository.delete(id);
        requestdetailSearchRepository.delete(id);
    }

    /**
     * Search for the requestdetail corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestDetail> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Requestdetails for query {}", query);
        Page<RequestDetail> result = requestdetailSearchRepository.search(queryStringQuery(query), pageable);
        return result;
    }
}
