/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.common.config.DefaultProfileUtil;
import nl.thehyve.podium.common.config.PodiumProperties;
import nl.thehyve.podium.common.security.annotations.Public;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Resource to return information about the currently running Spring profiles.
 */
@Public
@RestController
@RequestMapping("/api")
public class ProfileInfoResource {

    @Autowired
    private Environment env;

    @Autowired
    private PodiumProperties podiumProperties;

    @GetMapping("/profile-info")
    public ProfileInfoResponse getActiveProfiles() {
        String[] activeProfiles = DefaultProfileUtil.getActiveProfiles(env);
        return new ProfileInfoResponse(activeProfiles, getRibbonEnv(activeProfiles));
    }

    private String getRibbonEnv(String[] activeProfiles) {
        String[] displayOnActiveProfiles = podiumProperties.getRibbon().getDisplayOnActiveProfiles();

        if (displayOnActiveProfiles == null) {
            return null;
        }

        List<String> ribbonProfiles = new ArrayList<>(Arrays.asList(displayOnActiveProfiles));
        List<String> springBootProfiles = Arrays.asList(activeProfiles);
        ribbonProfiles.retainAll(springBootProfiles);

        if (ribbonProfiles.size() > 0) {
            return ribbonProfiles.get(0);
        }
        return null;
    }

    class ProfileInfoResponse {

        public String[] activeProfiles;
        public String ribbonEnv;

        ProfileInfoResponse(String[] activeProfiles, String ribbonEnv) {
            this.activeProfiles = activeProfiles;
            this.ribbonEnv = ribbonEnv;
        }
    }
}
