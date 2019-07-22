package com.docutools.users

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

import java.time.LocalDate

interface UserRepo extends JpaRepository<DocutoolsUser, UUID> {

    @Query("SELECT u FROM DocutoolsUser u WHERE UPPER(u.username) = UPPER(:username) OR UPPER(u.email) = UPPER(:username)")
    Optional<DocutoolsUser> findByUsernameIgnoreCase(@Param("username") String username)

    Optional<DocutoolsUser> findByVerificationStatusToken(String token)

    @Query(value = """SELECT * FROM docutools_users u 
                                        LEFT JOIN accounts account ON account.user_id=u.id 
                                        LEFT JOIN subscriptions subscription ON subscription.id=account.subscription_id 
             WHERE u.organisation_id = :org AND UPPER(CONCAT(u.first_name, ' ', u.last_name)) LIKE :search""",
             countQuery = """SELECT count(*) FROM docutools_users u 
                                        LEFT JOIN accounts a ON a.user_id=u.id 
                                        LEFT JOIN subscriptions s ON s.id=a.subscription_id 
             WHERE u.organisation_id = :org AND UPPER(CONCAT(u.first_name, ' ', u.last_name)) LIKE :search""",
             nativeQuery = true)
    Page<DocutoolsUser> findByOrganisationId(@Param('org') UUID organisationId,
                                             @Param('search') String search,
                                             Pageable pageable)


    @Query("""SELECT DISTINCT dtu FROM DocutoolsUser dtu LEFT JOIN TeamMembership tmb  ON  dtu.id = tmb.user.id
              WHERE (dtu.organisation.id =:orgId OR tmb.projectId IN :projectIds) 
               AND ( UPPER(CONCAT(dtu.name.firstName, ' ', dtu.name.lastName)) LIKE :search OR UPPER(dtu.username) LIKE :search)""")
    Page<DocutoolsUser> findUsers(
            @Param('projectIds') List<UUID> projectIds,
            @Param('orgId') UUID orgId, @Param('search') String search, Pageable pageable)

    long countByOrganisationName(OrganisationName organisationName)

    //Hibernate keeps intermediate proxy objects to lazy load on demand, So to load Account and Subscription Eagerly used Joins
    //Used CASE so do not need to write exact invert expression of Licensed
    @Query("""SELECT u FROM DocutoolsUser u LEFT JOIN Account a ON a.user=u LEFT JOIN Subscription s ON s=a.subscription
               WHERE u.organisation = :org AND UPPER(CONCAT(u.name.firstName, ' ', u.name.lastName)) LIKE :search
               AND ((CASE WHEN ((u.id = u.organisation.owner.id AND a IS NULL) 
                           OR (a IS NOT NULL AND (s.until IS NULL or s.until > :currentDate)))
                            THEN true ELSE false END) = :isLicensed )""")
    Page<DocutoolsUser> findByOrganisationAndLicense(@Param('org') Organisation organisation,
                                                     @Param('search') String search,
                                                     @Param('currentDate') LocalDate currentDate,
                                                     @Param('isLicensed') Boolean isLicensed,
                                                     Pageable pageable)

    @Query(value = """SELECT * FROM docutools_users u 
                                        LEFT JOIN accounts a ON a.user_id=u.id 
                                        LEFT JOIN subscriptions sa ON sa.id=a.subscription_id 
                                        LEFT JOIN subscriptions s ON s.organisation_id=u.organisation_id
               WHERE (a IS NOT NULL AND sa.type = 'Test' AND sa.until = :notificationPeriod)
                      OR (a IS NULL AND s.type = 'Test'  AND s.until = :notificationPeriod)""", nativeQuery = true)
    List<DocutoolsUser> findTestUserLicenceExpiringIn(@Param('notificationPeriod') LocalDate notificationPeriod)


}