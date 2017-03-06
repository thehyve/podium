/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.gateway;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TokenRelayFilter extends ZuulFilter {
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();

        Set<String> headers = (Set<String>) ctx.get("ignoredHeaders");
        // We need our JWT tokens relayed to resource servers
        headers.remove("authorization");

        return null;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10000;
    }
}
