package com.docutools.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganisationNameRepository extends JpaRepository<OrganisationName, UUID> {

    Optional<OrganisationName> findOneByOrganisationIdAndNameIgnoreCase(UUID orgId, String name);

}
