# Changelog

## [1.0.7]
- Return all active organisations instead of just the first page of 20. (Resolves issue [PODIUM-307](https://thehyve.atlassian.net/browse/PODIUM-307).)

## [1.0.6]
- Requests to public endpoints no longer send the Authorization header;
- Login issue fixed (an expired/invalid auth token no longer breaks the app);

## [1.0.5]
- Unnecessary error message removed from the Organization Form view;
- Podium Admin no longer can access organization management pages;

## [1.0.4]
- Angular updated from `4.4.7` to `11.2.5`;
- Spring Boot updated from `1.5.22.RELEASE` to `2.3.11.RELEASE`;
- Flowable updated from `6.0.0` to `6.6.0`;
- Minimum Node.js version bumped to `14.16.1`;
- Bugs fixed:
  * User deletion no longer breaks the organisation permission view;
  * Request details page shows all reviews for users with mixed reviewer+coordinator permissions;
  * BBMRI admins no longer receive a request to validate a new user when the user was created by an admin;
- JHipster Translate dropped in favor of Ngx-translate.
  (Resolves [issue #300](https://github.com/thehyve/podium/issues/300))

## [1.0.2]
Dependencies updates.

## [1.0.1]
Fix for issue [#279](https://github.com/thehyve/podium/issues/279).

## [1.0.0]
First official release.
