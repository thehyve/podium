/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.gateway.ratelimiting;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.config.PodiumProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;

/**
 * Zuul filter for limiting the number of HTTP calls per client.
 */
public class RateLimitingFilter extends ZuulFilter {

    private final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final String TIME_PERIOD = "hour";

    private long rateLimit = 100000L;

    private final RateLimitingRepository rateLimitingRepository;

    public RateLimitingFilter(RateLimitingRepository rateLimitingRepository, PodiumProperties podiumProperties) {
        this.rateLimitingRepository = rateLimitingRepository;
        this.rateLimit = podiumProperties.getGateway().getRateLimiting().getLimit();
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        // specific APIs can be filtered out using
        // if (RequestContext.getCurrentContext().getRequest().getRequestURI().startsWith("/api")) { ... }
        return true;
    }

    @Override
    public Object run() {
        String id = getId(RequestContext.getCurrentContext().getRequest());
        Date date = getPeriod();

        // check current rate limit
        // default limit per user is 100,000 API calls per hour
        Long count = rateLimitingRepository.getCounter(id, TIME_PERIOD, date);
        log.debug("Rate limiting for user {} at {} - {}",  id, date, count);
        if (count > rateLimit) {
            apiLimitExceeded();
        } else {
            // count calls per hour
            rateLimitingRepository.incrementCounter(id, TIME_PERIOD, date);
        }
        return null;
    }

    private void apiLimitExceeded() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody("API rate limit exceeded");
            ctx.setSendZuulResponse(false);
        }
    }

    /**
     * The ID that will identify the limit: the user login or the user IP address.
     */
    private String getId(HttpServletRequest httpServletRequest) {
        String login = SecurityService.getCurrentUserLogin();
        if (login != null) {
            return login;
        } else {
          return httpServletRequest.getRemoteAddr();
        }
    }

    /**
     * The period for which the rate is calculated.
     */
    private Date getPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.MILLISECOND);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MINUTE);
        return calendar.getTime();
    }
}
