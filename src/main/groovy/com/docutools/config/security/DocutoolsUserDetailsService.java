package com.docutools.config.security;

import com.docutools.password.PasswordPolicies;
import com.docutools.password.PasswordPolicy;
import com.docutools.users.UserRepo;
import com.docutools.users.values.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of {@link UserDetailsService} for the docutools specific security context.
 *
 * @since 1.0
 * @author amp
 */
@Service
public class DocutoolsUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(DocutoolsUserDetailsService.class);

    @Autowired
    private UserRepo repo;
    @Autowired
    private PasswordPolicies passwordPolicies;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByUsernameIgnoreCase(username)
            .map(user -> {
                if(user.getType() == UserType.Password && (user.getVerificationStatus() == null || !user.getVerificationStatus().isVerified())) {
                    throw new UsernameNotFoundException(username);
                }
                DocutoolsUserDetails userDetails = new DocutoolsUserDetails(user);

                PasswordPolicy passwordPolicy = passwordPolicies.get(user.getOrganisation().getPasswordPolicy());
                return userDetails;
            })
            .orElseThrow(() -> {
                log.debug("Unknown username: {}", username);
                return new UsernameNotFoundException(username);
            });
    }

}
