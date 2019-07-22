package com.docutools.users

import com.docutools.customers.Customer
import com.docutools.users.DocutoolsUser
import com.docutools.users.Organisation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

import java.util.stream.Stream

interface OrganisationRepo extends JpaRepository<Organisation, UUID> {

    // JPQL
    @Query("""SELECT m 
                FROM Organisation org 
                INNER JOIN org.members m 
                WHERE org.id = :organisationId 
                AND m.settings.admin IS TRUE""")
    Stream<DocutoolsUser> findOrganisationAdministrators(@Param("organisationId") UUID organisationId)


    @Query(value = """
        SELECT NEW com.docutools.customers.Customer(org.id, org.name, org.cc,
        CASE 
            WHEN org.billingMail <> '' THEN org.billingMail
            WHEN owner.email <> '' THEN owner.email 
            ELSE owner.username END,
        subscription, owner, org.created, org.reseller, 
        CASE WHEN credentials IS NULL THEN FALSE ELSE TRUE END)
        FROM Organisation org
        INNER JOIN DocutoolsUser owner ON org.owner.id = owner.id
        INNER JOIN Subscription subscription on subscription.organisation.id = org.id
        LEFT OUTER JOIN OAuth2ClientCredentials credentials ON CAST(org.id AS text) = credentials.clientId
        WHERE LOWER(org.name) LIKE LOWER(:search) 
            OR LOWER(org.billingMail) LIKE LOWER(:search) 
            OR LOWER(CONCAT(owner.name.firstName, ' ', owner.name.lastName)) LIKE LOWER(:search)
            OR LOWER(owner.email) LIKE LOWER(:search)
        """, countQuery = """
        SELECT COUNT(org)
        FROM Organisation org
        INNER JOIN DocutoolsUser owner ON org.owner.id = owner.id
        WHERE LOWER(org.name) LIKE LOWER(:search) 
            OR LOWER(org.billingMail) LIKE LOWER(:search) 
            OR LOWER(CONCAT(owner.name.firstName, ' ', owner.name.lastName)) LIKE LOWER(:search)
            OR LOWER(owner.email) LIKE LOWER(:search)
        """)
    Page<Customer> findByTerm(@Param('search') String search, Pageable pageable)

    @Query(nativeQuery = false, value = """
        SELECT org 
        FROM Organisation org
        INNER JOIN DocutoolsUser owner ON org.owner.id = owner.id
        WHERE LOWER(org.name) LIKE LOWER(:search) 
            OR LOWER(org.billingMail) LIKE LOWER(:search) 
            OR LOWER(CONCAT(owner.name.firstName, ' ', owner.name.lastName)) LIKE LOWER(:search)
            OR LOWER(owner.email) LIKE LOWER(:search)
        ORDER BY :sortBy 
        """)
    Stream<Organisation> findByTerm(@Param('search') String search, @Param('sortBy') String sortBy)

    Stream<Organisation> findByNameLike(String search, Sort sort)

    Optional<Organisation> findByIdpLink(String idpLink)

    @Query(
            """SELECT m
    FROM Organisation org
    INNER JOIN org.members m
    WHERE org.id = :organisationId""")
    Page<DocutoolsUser> findMembersByIdPaged(@Param("organisationId") UUID organisationId, Pageable pageable)
}
