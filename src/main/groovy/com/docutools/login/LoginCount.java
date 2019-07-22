package com.docutools.login;

import com.docutools.users.DocutoolsUser;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "login_counts")
public class LoginCount {

    @Id
    @Type(type = "pg-uuid")
    private UUID id;
    @OneToOne(optional = false)
    @JoinColumn(name = "id")
    private DocutoolsUser user;
    @Column(nullable = false)
    private int total = 0;

    public LoginCount() {
    }

    public LoginCount(DocutoolsUser user) {
        Assert.notNull(user, "user is required - must not be NULL!");
        Assert.notNull(user.getId(), "user's id is required - must not be NULL!");
        this.id = user.getId();
        this.user = user;
    }

    public DocutoolsUser getUser() {
        return user;
    }

    public UUID getId() {
        return id;
    }

    public int getTotal() {
        return total;
    }

    public int trackLogin() {
        return ++total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginCount that = (LoginCount) o;
        return total == that.total &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {

        return Objects.hash(user, total);
    }

    @Override
    public String toString() {
        return "LoginCount{" +
                "user=" + id +
                ", total=" + total +
                '}';
    }
}
