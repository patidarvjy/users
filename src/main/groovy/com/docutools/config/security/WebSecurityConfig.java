package com.docutools.config.security;

import com.docutools.config.security.otp.OTPAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import javax.servlet.http.HttpServletResponse;

/**
 * Configurations for Spring Web Security.
 *
 * @since 1.0
 * @author amp
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
        .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/api/v2/customers/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/oauth/**").permitAll()
                .antMatchers(HttpMethod.GET, "/saml/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/internal/v2/**").permitAll()
                .antMatchers("/**").authenticated()
                .and()
                .httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(otpAuthenticationProvider);
    }

    @Autowired
    private OTPAuthenticationProvider otpAuthenticationProvider;

}
