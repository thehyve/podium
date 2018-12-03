/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.common.service.dto.RequestFileRepresentation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static nl.thehyve.podium.web.rest.RequestDataHelper.setRequestData;

/**
 * Test class for the RequestFileResource REST controller.
 *
 * @see RequestFileResource
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestFileResourceIntTest extends AbstractRequestDataIntTest {

    @Test
    public void uploadFileToDraft() throws Exception {
        initMocks();
        RequestRepresentation request = newDraft(requester);
        setRequestData(request);
        request = updateDraft(requester, request);

        uploadRequestFile(requester, request, "test/images/KCOV_Excelsior_-_MP_2008.png");

        List<RequestFileRepresentation> files = getRequestFiles(requester, request);
        Assert.assertEquals(1, files.size());
        for (RequestFileRepresentation file: files) {
            Assert.assertEquals("KCOV_Excelsior_-_MP_2008.png", file.getFileName());
        }
    }

}
