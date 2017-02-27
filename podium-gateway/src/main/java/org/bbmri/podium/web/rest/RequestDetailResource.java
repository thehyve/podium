package org.bbmri.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.bbmri.podium.domain.RequestDetail;
import org.bbmri.podium.service.RequestDetailService;
import org.bbmri.podium.web.rest.util.HeaderUtil;
import org.bbmri.podium.web.rest.util.PaginationUtil;
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
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing RequestDetail.
 */
@RestController
@RequestMapping("/api")
public class RequestDetailResource {

    private final Logger log = LoggerFactory.getLogger(RequestDetailResource.class);

    private static final String ENTITY_NAME = "requestdetail";

    private final RequestDetailService requestDetailService;

    public RequestDetailResource(RequestDetailService requestDetailService) {
        this.requestDetailService = requestDetailService;
    }

    /**
     * POST  /requestdetails : Create a new requestDetail.
     *
     * @param requestDetail the requestDetail to create
     * @return the ResponseEntity with status 201 (Created) and with body the new requestDetail, or with status 400 (Bad Request) if the requestDetail has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/requestdetails")
    @Timed
    public ResponseEntity<RequestDetail> createRequestdetail(@Valid @RequestBody RequestDetail requestDetail) throws URISyntaxException {
        log.debug("REST request to save RequestDetail : {}", requestDetail);
        if (requestDetail.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new requestDetail cannot already have an ID")).body(null);
        }
        RequestDetail result = requestDetailService.save(requestDetail);
        return ResponseEntity.created(new URI("/api/requestdetails/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /requestdetails : Updates an existing requestDetail.
     *
     * @param requestDetail the requestDetail to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated requestDetail,
     * or with status 400 (Bad Request) if the requestDetail is not valid,
     * or with status 500 (Internal Server Error) if the requestDetail couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/requestdetails")
    @Timed
    public ResponseEntity<RequestDetail> updateRequestdetail(@Valid @RequestBody RequestDetail requestDetail) throws URISyntaxException {
        log.debug("REST request to update RequestDetail : {}", requestDetail);
        if (requestDetail.getId() == null) {
            return createRequestdetail(requestDetail);
        }
        RequestDetail result = requestDetailService.save(requestDetail);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, requestDetail.getId().toString()))
            .body(result);
    }

    /**
     * GET  /requestdetails : get all the requestdetails.
     *
     * @param pageable the pagination information
     * @param filter the filter of the request
     * @return the ResponseEntity with status 200 (OK) and the list of requestdetails in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/requestdetails")
    @Timed
    public ResponseEntity<List<RequestDetail>> getAllRequestdetails(@ApiParam Pageable pageable, @RequestParam(required = false) String filter)
        throws URISyntaxException {
        if ("request-is-null".equals(filter)) {
            log.debug("REST request to get all Requestdetails where request is null");
            return new ResponseEntity<>(requestDetailService.findAllWhereRequestIsNull(),
                    HttpStatus.OK);
        }
        log.debug("REST request to get a page of Requestdetails");
        Page<RequestDetail> page = requestDetailService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/requestdetails");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /requestdetails/:id : get the "id" requestdetail.
     *
     * @param id the id of the requestdetail to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the requestdetail, or with status 404 (Not Found)
     */
    @GetMapping("/requestdetails/{id}")
    @Timed
    public ResponseEntity<RequestDetail> getRequestdetail(@PathVariable Long id) {
        log.debug("REST request to get RequestDetail : {}", id);
        RequestDetail requestDetail = requestDetailService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(requestDetail));
    }

    /**
     * DELETE  /requestdetails/:id : delete the "id" requestdetail.
     *
     * @param id the id of the requestdetail to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/requestdetails/{id}")
    @Timed
    public ResponseEntity<Void> deleteRequestdetail(@PathVariable Long id) {
        log.debug("REST request to delete RequestDetail : {}", id);
        requestDetailService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/requestdetails?query=:query : search for the requestdetail corresponding
     * to the query.
     *
     * @param query the query of the requestdetail search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/requestdetails")
    @Timed
    public ResponseEntity<List<RequestDetail>> searchRequestdetails(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Requestdetails for query {}", query);
        Page<RequestDetail> page = requestDetailService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/requestdetails");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
