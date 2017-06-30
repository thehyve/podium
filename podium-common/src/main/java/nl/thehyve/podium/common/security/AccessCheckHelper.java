package nl.thehyve.podium.common.security;

import nl.thehyve.podium.common.exceptions.AccessDenied;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for access checks.
 */
public class AccessCheckHelper {

    /**
     * Checks if the user has the requested authority for at least one of the organisations with the specified
     * organisation uuids.
     * @param user the user object.
     * @param organisationUuids the collection of organisation uuids.
     * @param authority the requested authority.
     * @throws AccessDenied iff the user does not have the required access rights.
     */
    public static void checkOrganisationAccess(AuthenticatedUser user, Collection<UUID> organisationUuids, String authority) {
        for (UUID organisationUuid: organisationUuids) {
            Collection<String> organisationAuthorities = user.getOrganisationAuthorities().get(organisationUuid);
            if (organisationAuthorities != null && organisationAuthorities.contains(authority)) {
                // the authority is present for one of the organisations
                return;
            }
        }
        throw new AccessDenied("Access denied for organisations " + Arrays.toString(organisationUuids.toArray()));
    }

    /**
     * Checks if the user has the requested authority for the organisation with the specified
     * organisation uuid.
     * @param user the user object.
     * @param organisationUuid the uuid of the organisation.
     * @param authority the requested authority.
     * @throws AccessDenied iff the user does not have the requested access.
     */
    public static void checkOrganisationAccess(AuthenticatedUser user, UUID organisationUuid, String authority) {
        checkOrganisationAccess(user, Collections.singleton(organisationUuid), authority);
    }

    /**
     * Returns the array of uuids of the organisations for which the user has the roles with given authority.
     *
     * @param user the user.
     * @param authority the authority to filter on.
     * @return the array of organisation uuids.
     */
    public static UUID[] getOrganisationUuidsForUserAndRole(AuthenticatedUser user, String authority) {
        Collection<UUID> organisationUuids = user.getOrganisationAuthorities().entrySet().stream()
            .filter(entry -> entry.getValue().contains(authority))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        return organisationUuids.toArray(new UUID[0]);
    }

}
