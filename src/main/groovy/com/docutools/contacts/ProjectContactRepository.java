package com.docutools.contacts;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface ProjectContactRepository extends JpaRepository<ProjectContact, UUID> {

    @Query(nativeQuery = true, value = "SELECT * FROM project_contacts pc WHERE pc.project_id = :projectId AND pc.replaced IS FALSE AND " +
            "CONCAT(pc.email, ' ', pc.company_name, ' ', pc.first_name, ' ', pc.last_name, ' ', " +
            "pc.phone, ' ', pc.fax, ' ', pc.job_title, ' ', pc.department, ' ', pc.internal_id, ' '" +
            ", pc.street, ' ', pc.city, ' ', pc.country_code) ILIKE ('%' || :search || '%')")
    Stream<ProjectContact> searchInProject(@Param("projectId") UUID projectId, @Param("search") String search);

    @Query(nativeQuery = true,
            value = "SELECT * FROM project_contacts pc WHERE pc.project_id = :projectId AND pc.replaced IS FALSE AND " +
                    "CONCAT(pc.email, ' ', pc.company_name, ' ', pc.first_name, ' ', pc.last_name, ' ', " +
                    "pc.phone, ' ', pc.fax, ' ', pc.job_title, ' ', pc.department, ' ', pc.internal_id, ' '" +
                    ", pc.street, ' ', pc.city, ' ', pc.country_code) ILIKE ('%' || :search || '%')",
            countQuery = "SELECT count(*) FROM project_contacts pc WHERE pc.project_id = :projectId AND pc.replaced IS FALSE AND " +
                    "CONCAT(pc.email, ' ', pc.company_name, ' ', pc.first_name, ' ', pc.last_name, ' ', " +
                    "pc.phone, ' ', pc.fax, ' ', pc.job_title, ' ', pc.department, ' ', pc.internal_id, ' '" +
                    ", pc.street, ' ', pc.city, ' ', pc.country_code) ILIKE ('%' || :search || '%')")
    Stream<ProjectContact> searchInProject(@Param("projectId") UUID projectId, @Param("search") String search, Pageable pageable);

    Stream<ProjectContact> findByProjectId(UUID projectId);

}
