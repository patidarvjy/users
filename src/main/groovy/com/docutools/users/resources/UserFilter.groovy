package com.docutools.users.resources

/**
 * Allows to filter {@link UserDTO}s by their licensing state.
 */
enum UserFilter {
    Any,
    Licensed,
    WithoutLicense
}