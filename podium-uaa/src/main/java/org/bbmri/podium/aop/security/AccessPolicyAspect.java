/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package org.bbmri.podium.aop.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.bbmri.podium.common.security.annotations.*;
import org.bbmri.podium.service.AccessPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aspect to enforce access policy for controller methods based on custom security annotations.
 * The policy is enforced on all classes in the package <code>org.bbmri.podium.web.rest</code>.
 * For the annotations, see {@link Public}, {@link AnyAuthorisedUser}, {@link SecuredByAuthority},
 * {@link SecuredByOrganisation}, and {@link SecuredByCurrentUser}.
 *
 * The annotations can be applied both on class and on method level. If a method is annotated, the class
 * annotations will be ignored for that method. If a methods is not annotated, the class annotations
 * will be used instead.
 *
 * If a user tries to access a method for which no rule is satisfied, the advice throws an
 * {@link AccessDeniedException}.
 * The annotations are interpreted disjunctively: at least one of the rules needs to be satisfied.
 */
@Aspect
public class AccessPolicyAspect {

    private final Logger log = LoggerFactory.getLogger(AccessPolicyAspect.class);

    @Autowired
    AccessPolicyService accessPolicyService;

    /**
     * Pointcut that matches all Web REST endpoints in the <code>org.bbmri.podium.web.rest</code> package.
     */
    @Pointcut("within(org.bbmri.podium.web.rest..*)")
    public void controllersPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    private Collection<Annotation> getClassAnnotations(JoinPoint joinPoint) {
        Signature signature = joinPoint.getStaticPart().getSignature();
        Class type = signature.getDeclaringType();
        return Arrays.stream(type.getAnnotations())
            .filter(AccessPolicyService::isSecurityAnnotation)
            .collect(Collectors.toList());
    }

    private Collection<Annotation> getMethodAnnotations(JoinPoint joinPoint) {
        Signature signature = joinPoint.getStaticPart().getSignature();
        Class type = signature.getDeclaringType();
        Optional<Method> methodOptional = Arrays.stream(type.getMethods()).filter(m ->
            signature.getName().equals(m.getName())
        ).findAny();
        if (!methodOptional.isPresent()) {
            log.error("No method {} found in class {}.", signature.getName(), signature.getDeclaringTypeName());
            return Collections.emptyList();
        }
        Method method = methodOptional.get();
        return Arrays.stream(method.getAnnotations())
            .filter(AccessPolicyService::isSecurityAnnotation)
            .collect(Collectors.toList());
    }

    /**
     * Before execution of the method, check if any security rule for the method is satisfied.
     * Throws an {@link AccessDeniedException} if not.
     */
    @Before("controllersPointcut()")
    public void checkAccess(JoinPoint joinPoint) {
        log.info("Checking access policy on {}", joinPoint.getSignature().toShortString());
        Collection<Annotation> methodAnnotations = getMethodAnnotations(joinPoint);
        if (!methodAnnotations.isEmpty()) {
            for (Annotation annotation : methodAnnotations) {
                log.info("Checking security method annotation: {}", annotation);
                if (accessPolicyService.checkSecurityAnnotation(annotation, joinPoint)) {
                    return;
                }
            }
            log.info("Access denied: no method level security rules are satisfied.");
            throw new AccessDeniedException("Access denied.");
        }
        Collection<Annotation> classAnnotations = getClassAnnotations(joinPoint);
        if (!classAnnotations.isEmpty()) {
            for (Annotation annotation : classAnnotations) {
                log.info("Checking security class annotation: {}", annotation);
                if (accessPolicyService.checkSecurityAnnotation(annotation, joinPoint)) {
                    return;
                }
            }
            log.info("Access denied: no class level security rules are satisfied.");
            throw new AccessDeniedException("Access denied.");
        }
        log.info("Access denied: no method level or class level security rules found.");
        throw new AccessDeniedException("Access denied.");
    }

}
