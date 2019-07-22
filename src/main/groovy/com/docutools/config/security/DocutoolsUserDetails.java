package com.docutools.config.security;

import com.docutools.users.DocutoolsUser;
import com.docutools.utils.Validator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Custom implementation of {@link UserDetails} for the docutools security context. Call {@link this#getDocutoolsUser()}
 * to get the {@link DocutoolsUser} instance for this user.
 *
 * @since 1.0
 * @author amp
 */
public class DocutoolsUserDetails implements UserDetails {

    /**
     * Lists all {@link GrantedAuthority} instances for the given {@link DocutoolsUser} supports by this application.
     *
     * @param user the {@link DocutoolsUser}
     * @return list of {@link GrantedAuthority} instances.
     */
    public static List<GrantedAuthority> buildAuthorities(DocutoolsUser user) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if(user.getSettings().isAdmin()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("admin"));
        }
        if(user.getSettings().isProjectCreator()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("project_creator"));
        }
        if(user.isOrganisationOwner()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("owner"));
        }
        if(Validator.isSustainUser(user.getUsername())){
            grantedAuthorities.add(new SimpleGrantedAuthority("sustain_user"));
        }
        return grantedAuthorities;
    }

    private DocutoolsUser user;
    private List<GrantedAuthority> grantedAuthorities;
    private boolean passwordNotExpired = true;

    /**
     * Creates a new {@link UserDetails} instance based on the provided {@link DocutoolsUser}.
     *
     * @param user base for this principal
     */
    public DocutoolsUserDetails(DocutoolsUser user) {
        Assert.notNull(user);
        this.user = user;
        // Set granted authorities
        this.grantedAuthorities = buildAuthorities(user);
    }

    /**
     * Gets the database entity for this user.
     *
     * @return database entity
     */
    public DocutoolsUser getDocutoolsUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword().getHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * When this is {@link true}, the authentication will fail with status code {@code 401}
     * and include "User's credentials expired." in the body.
     *
     * @return {@link true} when expired.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return passwordNotExpired;
    }

    public void setPasswordNotExpired(boolean passwordNotExpired) {
        this.passwordNotExpired = passwordNotExpired;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    /**
     * Gets the unique ID of this user.
     *
     * @return {@link UUID}
     */
    public UUID getId() {
        return user.getId();
    }
}
