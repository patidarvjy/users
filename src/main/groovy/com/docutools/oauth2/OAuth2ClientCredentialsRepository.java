package com.docutools.oauth2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuth2ClientCredentialsRepository extends JpaRepository<OAuth2ClientCredentials, String> {
}
