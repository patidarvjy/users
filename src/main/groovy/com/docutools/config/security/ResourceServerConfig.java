package com.docutools.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * Configuring this application as OAuth2 resource server.
 *
 * @since 1.0
 * @author amp
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                //.cors().configurationSource(corsConfigurationSource()).and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/saml/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v2/twoFactor").permitAll()
                .antMatchers(HttpMethod.GET, "/mfa-api/v1/sms").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/api/v2/customers/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/oauth/**").permitAll()
                .antMatchers("/api/v2/register/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v2/users/email/2fa").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v2/passwordReset").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v2/users/email").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v2/me/email/verify").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v2/users/unsubscribe").permitAll()
                .antMatchers(HttpMethod.GET, "/api/internal/v2/**").permitAll()
                // Legacy
                .antMatchers(HttpMethod.POST, "/api/v2/users/email").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v2/me/verify").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v2/me/passwordReset").permitAll()
                .antMatchers("/api/v2/**").authenticated();
    }

    /*@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }*/


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources
                .resourceId("users")
                .tokenStore(tokenStore);
    }

    @Autowired
    TokenStore tokenStore;
}
