/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.aop.security;

import nl.thehyve.podium.common.security.annotations.AnyAuthorisedUser;
import nl.thehyve.podium.common.security.annotations.Public;
import nl.thehyve.podium.common.security.annotations.SecuredByAuthority;
import nl.thehyve.podium.common.security.annotations.SecuredByCurrentUser;
import nl.thehyve.podium.common.security.annotations.SecuredByOrganisation;
import nl.thehyve.podium.common.service.AccessPolicyService;
import nl.thehyve.podium.common.service.SecurityService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aspect to enforce access policy for controller methods based on custom security annotations.
 * The policy is enforced on all classes in the package <code>nl.thehyve.podium.web.rest</code>.
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

    @Autowired
    SecurityService securityService;

    /**
     * Pointcut that matches all Web REST endpoints in the <code>nl.thehyve.podium.web.rest</code> package.
     */
    @Pointcut("within(nl.thehyve.podium.web.rest..*)")
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

    private boolean hasAccess(JoinPoint joinPoint) {
        Collection<Annotation> methodAnnotations = getMethodAnnotations(joinPoint);
        if (!methodAnnotations.isEmpty()) {
            for (Annotation annotation : methodAnnotations) {
                log.debug("Checking security method annotation: {}", annotation);
                if (accessPolicyService.checkSecurityAnnotation(annotation, joinPoint)) {
                    return true;
                }
            }
            log.debug("Access denied: no method level security rules are satisfied.");
            return false;
        }
        Collection<Annotation> classAnnotations = getClassAnnotations(joinPoint);
        if (!classAnnotations.isEmpty()) {
            for (Annotation annotation : classAnnotations) {
                log.debug("Checking security class annotation: {}", annotation);
                if (accessPolicyService.checkSecurityAnnotation(annotation, joinPoint)) {
                    return true;
                }
            }
            log.debug("Access denied: no class level security rules are satisfied.");
            throw new AccessDeniedException("Access denied.");
        }
        log.debug("Access denied: no method level or class level security rules found.");
        return false;
    }

    private String formatRequest(JoinPoint joinPoint) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            UUID objectUuid = null;
            if (request.getMethod().equals("PUT")) {
                // Get UUID from request body
                objectUuid = accessPolicyService.getSecuredObjectUuid(joinPoint);
            }
            return request.getMethod() + " " + request.getRequestURI() +
                (objectUuid == null ? "" : " (object UUID: " + objectUuid + ")");
        }
        return joinPoint.getSignature().toShortString();
    }

    /**
     * Before execution of the method, check if any security rule for the method is satisfied.
     * Throws an {@link AccessDeniedException} if not.
     *
     * @param joinPoint Candidate point in application execution where the aspect can be plugged in.
     */
    @Before("controllersPointcut()")
    public void checkAccess(JoinPoint joinPoint) {
        UUID currentUserUuid = securityService.getCurrentUserUuid();
        log.debug("Checking access for user {} on {}", currentUserUuid, formatRequest(joinPoint));
        if (hasAccess(joinPoint)) {
            log.info("Access granted to user {} on {}", currentUserUuid, formatRequest(joinPoint));
            return;
        }
        log.info("Access denied to user {} on {}", currentUserUuid, formatRequest(joinPoint));
        throw new AccessDeniedException("Access denied.");
    }

}
