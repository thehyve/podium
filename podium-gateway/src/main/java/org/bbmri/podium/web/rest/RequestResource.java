package org.bbmri.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.bbmri.podium.service.RequestService;
import org.bbmri.podium.web.rest.util.HeaderUtil;
import org.bbmri.podium.web.rest.util.PaginationUtil;
import org.bbmri.podium.service.dto.RequestDTO;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Request.
 */
@RestController
@RequestMapping("/api")
public class RequestResource {

    private final Logger log = LoggerFactory.getLogger(RequestResource.class);

    private static final String ENTITY_NAME = "request";
        
    private final RequestService requestService;

    public RequestResource(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * POST  /requests : Create a new request.
     *
     * @param requestDTO the requestDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new requestDTO, or with status 400 (Bad Request) if the request has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/requests")
    @Timed
    public ResponseEntity<RequestDTO> createRequest(@Valid @RequestBody RequestDTO requestDTO) throws URISyntaxException {
        log.debug("REST request to save Request : {}", requestDTO);
        if (requestDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new request cannot already have an ID")).body(null);
        }
        RequestDTO result = requestService.save(requestDTO);
        return ResponseEntity.created(new URI("/api/requests/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /requests : Updates an existing request.
     *
     * @param requestDTO the requestDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated requestDTO,
     * or with status 400 (Bad Request) if the requestDTO is not valid,
     * or with status 500 (Internal Server Error) if the requestDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/requests")
    @Timed
    public ResponseEntity<RequestDTO> updateRequest(@Valid @RequestBody RequestDTO requestDTO) throws URISyntaxException {
        log.debug("REST request to update Request : {}", requestDTO);
        if (requestDTO.getId() == null) {
            return createRequest(requestDTO);
        }
        RequestDTO result = requestService.save(requestDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, requestDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /requests : get all the requests.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of requests in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/requests")
    @Timed
    public ResponseEntity<List<RequestDTO>> getAllRequests(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Requests");
        Page<RequestDTO> page = requestService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /requests/:id : get the "id" request.
     *
     * @param id the id of the requestDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the requestDTO, or with status 404 (Not Found)
     */
    @GetMapping("/requests/{id}")
    @Timed
    public ResponseEntity<RequestDTO> getRequest(@PathVariable Long id) {
        log.debug("REST request to get Request : {}", id);
        RequestDTO requestDTO = requestService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(requestDTO));
    }

    /**
     * DELETE  /requests/:id : delete the "id" request.
     *
     * @param id the id of the requestDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/requests/{id}")
    @Timed
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        log.debug("REST request to delete Request : {}", id);
        requestService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/requests?query=:query : search for the request corresponding
     * to the query.
     *
     * @param query the query of the request search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/requests")
    @Timed
    public ResponseEntity<List<RequestDTO>> searchRequests(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Requests for query {}", query);
        Page<RequestDTO> page = requestService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
