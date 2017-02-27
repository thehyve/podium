package org.bbmri.podium.service;

import org.bbmri.podium.domain.Request;
import org.bbmri.podium.domain.RequestDetail;
import org.bbmri.podium.domain.enumeration.RequestStatus;
import org.bbmri.podium.repository.RequestRepository;
import org.bbmri.podium.repository.search.RequestSearchRepository;
import org.bbmri.podium.service.dto.RequestDTO;
import org.bbmri.podium.service.mapper.RequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Request.
 */
@Service
@Transactional
public class RequestService {

    private final Logger log = LoggerFactory.getLogger(RequestService.class);

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private RequestSearchRepository requestSearchRepository;

    @Autowired
    private RequestDetailService requestDetailService;

    public RequestService() {
    }

    /**
     * Save a request.
     *
     * @param request the entity to save
     * @return the persisted entity
     */
    @Transactional
    public Request save(Request request) {
        return requestRepository.save(request);
    }

    /**
     *  Get all the requests.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Requests");
        Page<Request> result = requestRepository.findAll(pageable);
        return result.map(requestMapper::requestToRequestDTO);
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> findAllRequestDraftsByUserUuid(UUID uuid) {
        List<Request> result = requestRepository.findAllByRequesterAndStatus(uuid, RequestStatus.DRAFT);
        return requestMapper.requestsToRequestDTOs(result);
    }

    @Transactional
    public RequestDTO initializeBaseRequest(UUID requester) {
        Request request = new Request();
        request.setStatus(RequestStatus.DRAFT);
        request.setRequester(requester);

        // RequestDetail requestDetail = new RequestDetail();

        // requestDetailService.save(requestDetail);
        // request.setRequestDetail(requestDetail);

        save(request);
        return requestMapper.requestToRequestDTO(request);
    }

    /**
     *  Delete the  request by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Request : {}", id);
        requestRepository.delete(id);
        requestSearchRepository.delete(id);
    }

    /**
     * Search for the request corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Requests for query {}", query);
        Page<Request> result = requestSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(request -> requestMapper.requestToRequestDTO(request));
    }
}
