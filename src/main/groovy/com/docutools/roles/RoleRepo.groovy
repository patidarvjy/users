package com.docutools.roles

import com.docutools.users.Organisation
import com.docutools.roles.Role
import com.docutools.roles.RoleType
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * {@link JpaRepository} for {@link Role}s.
 */
interface RoleRepo extends JpaRepository<Role, UUID> {

    Optional<Role> findById(UUID id)
    Optional<Role> findByRoleTypeAndOrganisation(RoleType roleType, Organisation organisation)
    List<Role> findByOrganisation(Organisation organisation, Sort sort)

    @Query(nativeQuery = true, value = """
SELECT COUNT(p)
FROM (SELECT DISTINCT tm.project_id
        FROM role_assignments ra
        INNER JOIN team_memberships tm ON ra.member_id = tm.id
        WHERE ra.role_id = :roleId) p
""")
    long countUsage(@Param('roleId') UUID roleId)

}