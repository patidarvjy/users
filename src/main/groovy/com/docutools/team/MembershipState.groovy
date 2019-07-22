package com.docutools.team

/**
 * State a member has in a team.
 */
enum MembershipState {

    /**
     * The user account is active and is part of the team.
     */
    Active,
    /**
     * The user account is inactive or the membership was set inactive.
     */
    Inactive,
    /**
     * The user got invited to a project but did not accept yet.
     */
    Invited,
    /**
     * No longer listed as member of the project.
     */
    Removed,

}