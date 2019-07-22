package com.docutools.login;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoginCountRepository extends JpaRepository<LoginCount,UUID> {
}
