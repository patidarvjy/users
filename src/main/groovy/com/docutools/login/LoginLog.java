package com.docutools.login;

import com.docutools.users.DocutoolsUser;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A successful login log for a given {@link DocutoolsUser}.
 *
 * @author amp
 */
@Entity
@Table(name = "login_logs")
public class LoginLog {

    @Id
    @Type(type = "pg-uuid")
    private UUID id = UUID.randomUUID();
    @CreatedDate
    private ZonedDateTime created = ZonedDateTime.now();
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private DocutoolsUser user;

    public LoginLog() {
    }

    public LoginLog(DocutoolsUser user) {
        Assert.notNull(user, "user is required - must not be NULL!");
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public DocutoolsUser getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginLog loginLog = (LoginLog) o;
        return Objects.equals(id, loginLog.id) &&
                Objects.equals(created, loginLog.created);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, created);
    }

    @Override
    public String toString() {
        return "LoginLog{" +
                "id=" + id +
                ", created=" + created +
                '}';
    }
}
