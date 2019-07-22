package com.docutools.team

import com.docutools.users.DocutoolsUser
import com.docutools.users.Organisation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

import java.time.ZonedDateTime
import java.util.stream.Stream

/**
 * {@link JpaRepository} for {@link TeamMembership}.
 */
interface TeamMembershipRepo extends JpaRepository<TeamMembership, UUID> {
    @Query('SELECT tm FROM TeamMembership tm WHERE tm.projectId = :id AND tm.user = :user AND tm.state != \'Removed\'')
    Optional<TeamMembership> findMember(@Param("user") DocutoolsUser user, @Param("id") UUID projectId)

    @Query('SELECT tm FROM TeamMembership tm WHERE tm.projectId = :id AND tm.user = :user')
    Optional<TeamMembership> findMemberAnyState(@Param("user") DocutoolsUser user, @Param("id") UUID projectId)

    Stream<TeamMembership> findByProjectId(UUID projectId);
    Stream<TeamMembership> findByProjectIdAndStateIsNot(UUID projectId, MembershipState excludeState)

    @Query('SELECT tm FROM TeamMembership tm WHERE tm.projectId = :id AND tm.state != \'Removed\'')
    List<TeamMembership> findTeam(@Param('id') UUID project)

    @Query('SELECT tm FROM TeamMembership tm WHERE tm.projectId = :id AND tm.state in :states')
    List<TeamMembership> findTeamByState(@Param('id') UUID project, @Param('states') List<MembershipState> states)

    @Query('SELECT tm FROM TeamMembership tm WHERE tm.projectId = :id AND tm.user.organisation = :org')
    List<TeamMembership> filterTeamByMyCompany(@Param('id') UUID project, @Param('org') Organisation org)

    @Query('SELECT tm FROM TeamMembership tm WHERE tm.projectId = :id AND tm.user.organisation = :org AND tm.state in :states')
    List<TeamMembership> filterTeamByMyCompanyAndState(@Param('id') UUID project, @Param('org') Organisation org, @Param('states') List<MembershipState> states)

    @Query('SELECT tm FROM TeamMembership tm WHERE tm.projectId = :id AND tm.user.organisation != :org ')
    List<TeamMembership> filterByOtherCompanies(@Param('id') UUID project, @Param('org') Organisation org)

    @Query('SELECT tm FROM TeamMembership tm WHERE tm.projectId = :id AND tm.user.organisation != :org AND tm.state in :states')
    List<TeamMembership> filterByOtherCompaniesAndState(@Param('id') UUID project, @Param('org') Organisation org, @Param('states') List<MembershipState> states)

    @Query(value = """select t.* from docutools_users AS u inner join team_memberships AS t on t.user_id = u.id 
                    inner join organisations as o on o.id = u.organisation_id 
                    where t.project_id=:projectId AND t.state='Active' AND (u.first_name ilike %:searchText% OR 
                    u.last_name ilike %:searchText% OR u.username ilike %:searchText%
                    OR u.job_title ilike %:searchText% OR o.name  ilike %:searchText%)""", nativeQuery = true)
    List<TeamMembership> searchTeamMembers(@Param('projectId') UUID project,
                                           @Param('searchText') String searchText)

    @Query(value = """select t.* from docutools_users AS u inner join team_memberships AS t on t.user_id = u.id 
                    inner join organisations as o on o.id = u.organisation_id 
                    where t.project_id=:projectId AND t.state='Active' AND (u.first_name ilike %:searchText% OR 
                    u.last_name ilike %:searchText% OR u.username ilike %:searchText%
                    OR u.job_title ilike %:searchText% OR o.name  ilike %:searchText%)""",
            countQuery = """select count(t.*) from docutools_users AS u inner join team_memberships AS t on t.user_id = u.id 
                    inner join organisations as o on o.id = u.organisation_id 
                    where t.project_id=:projectId AND t.state='Active' AND (u.first_name ilike %:searchText% OR 
                    u.last_name ilike %:searchText% OR u.username ilike %:searchText%
                    OR u.job_title ilike %:searchText% OR o.name  ilike %:searchText%)""",
            nativeQuery = true)
    List<TeamMembership> searchTeamMembers(@Param('projectId') UUID project,
                                           @Param('searchText') String searchText,
                                           Pageable pageable)

    Stream<TeamMembership> findByUser(DocutoolsUser user)

    List<TeamMembership> findByUserId(UUID id)

    @Query(value = "select CAST(m.project_id AS VARCHAR) from team_memberships m, docutools_users u where m.user_id = u.id and u.id=:userId and m.state =:state and (m.role_id is not null or exists(select 1 from role_assignments r where r.member_id=m.id))", nativeQuery = true)
    Stream<String> findProjectIdListFromActiveMembershipsWithRolePresentForUser(@Param('userId') UUID userId, @Param('state') String state)

    @Query(value = "select CAST(m.project_id AS VARCHAR) from team_memberships m, docutools_users u where m.user_id = u.id and u.id=:userId and m.state =:state and (m.role_id is not null or exists(select 1 from role_assignments r where r.member_id=m.id))",
            countQuery = "select count(m.*) from team_memberships m, docutools_users u where m.user_id = u.id and u.id=:userId and m.state =:state and (m.role_id is not null or exists(select 1 from role_assignments r where r.member_id=m.id))",
            nativeQuery = true)
    Page<String> findProjectIdListFromActiveMembershipsWithRolePresentForUser(@Param('userId') UUID userId, @Param('state') String state, Pageable pageable)

    @Query(value = "select CAST(m.project_id AS VARCHAR) from team_memberships m, docutools_users u where m.user_id = u.id and u.id=:userId and m.state =:state and m.last_modified >:since and (m.role_id is not null or exists(select 1 from role_assignments r where r.member_id=m.id))", nativeQuery = true)
    Stream<String> findProjectIdListFromRemovedMembershipsSinceWithRolePresentForUser(@Param('userId') UUID userId, @Param('state') String state, @Param('since') ZonedDateTime since)

    @Query('SELECT tm FROM TeamMembership tm WHERE tm.projectId = :id AND tm.user.id = :userId')
    Optional<TeamMembership> findMembership(@Param("userId") UUID userId, @Param("id") UUID projectId)

    @Query(value = """select t.* from team_memberships t where t.user_id in :userIds and t.project_id= :projectId""", nativeQuery = true)
    List<TeamMembership> findMembers(@Param("userIds") List<UUID> userIds, @Param("projectId") UUID projectId)
}