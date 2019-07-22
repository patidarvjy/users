package com.docutools.password;

import com.docutools.users.DocutoolsUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PasswordUserRepoHelper extends CrudRepository<DocutoolsUser, UUID> {

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE docutools_users SET password_last_changed = :d WHERE id = :id")
    void expirePassword(@Param("id") UUID id, @Param("d") LocalDateTime dateTime);

}
