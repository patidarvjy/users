package com.docutools.config.security.otp;

import com.docutools.config.security.DocutoolsUserDetails;
import com.docutools.config.security.DocutoolsUserDetailsService;
import com.docutools.login.LoginTracker;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.values.UserSettings;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * A {@link DaoAuthenticationProvider} for OTP support.
 *
 * @since 1.0
 * @author amp
 */
@Service
public class OTPAuthenticationProvider extends DaoAuthenticationProvider {

    @Autowired
    private LoginTracker loginTracker;

    @Autowired
    public OTPAuthenticationProvider(DocutoolsUserDetailsService userDetailsService,
                                     Pbkdf2PasswordEncoder passwordEncoder) {
        super.setUserDetailsService(userDetailsService);
        super.setPasswordEncoder(passwordEncoder);
    }

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        if(auth instanceof PreAuthenticatedAuthenticationToken) {
            return auth;
        }
        Authentication authentication = super.authenticate(auth);
        DocutoolsUser user = ((DocutoolsUserDetails)authentication.getPrincipal()).getDocutoolsUser();
        UserSettings settings = user.getSettings();
        if(settings.getTwoFactorAuthEnabled() || settings.getSmsFactorAuthEnabled()) {
            LinkedHashMap<String, String> details = (LinkedHashMap<String, String>)auth.getDetails();

            String verificationCode = Optional.ofNullable(details.getOrDefault("code", null))
                    .orElseThrow(() -> new BadCredentialsException("missing verification code"));

            // The SMS delays, so SMS verification code is valid for 5 minutes. Otherwise use the default 30 seconds
            final int interval = settings.getSmsFactorAuthEnabled()
                    ? 300 //  5 minutes
                    : 30; // 30 seconds
            final Clock clock = new Clock(interval);
            Totp totp = new Totp(settings.getTwoFASecret(), clock);
            if(!totp.verify(verificationCode)) {
                throw new BadCredentialsException("Invalid verification code");
            }
        }
        loginTracker.trackLogin(user);
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class) ||
                authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }

}
