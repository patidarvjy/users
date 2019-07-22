package com.docutools.config.security;

import com.docutools.users.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.Arrays;

/**
 * Configurations for the OAuth2 authorization server.
 *
 * @author amp
 * @since 1.0
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationServerConfig.class);

    @Value("${docutools.security.jwt.keystore.resource:}")
    private String keystoreResource;
    @Value("${docutools.security.jwt.keystore.path:}")
    private String keystorePath;
    @Value("${docutools.security.jwt.keystore.storepass}")
    private String storepass;
    @Value("${docutools.security.jwt.keystore.keyname}")
    private String keyname;

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;
    @Autowired
    private DocutoolsUserDetailsService userDetailsService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepo userRepo;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(getJdbcClientDetailsService());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.checkTokenAccess("permitAll()");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(
                Arrays.asList(tokenEnhancer(userRepo), accessTokenConverter()));
        endpoints
                .tokenStore(tokenStore())
                .tokenServices(tokenServices(tokenStore(), authenticationManager, tokenEnhancerChain))
                .accessTokenConverter(accessTokenConverter())
                .tokenEnhancer(tokenEnhancerChain)
                .authenticationManager(authenticationManager).userDetailsService(userDetailsService)
                .exceptionTranslator(new DefaultWebResponseExceptionTranslator(){
                    @Override
                    public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
                        log.error(e.getMessage(), e);
                        return super.translate(e);
                    }
                });
    }

    @Bean
    @Autowired
    public CustomTokenServices tokenServices(TokenStore tokenStore, AuthenticationManager authenticationManager, TokenEnhancerChain tokenEnhancerChain) {
        CustomTokenServices tokenServices = new CustomTokenServices(tokenStore);
        tokenServices.setAuthenticationManager(authenticationManager);
        tokenServices.setTokenEnhancer(tokenEnhancerChain);
        tokenServices.setClientDetailsService(getJdbcClientDetailsService());
        return tokenServices;
    }

    @Bean
    @Autowired
    public DocuToolsTokenEnhancer tokenEnhancer(UserRepo userRepo) {
        return new DocuToolsTokenEnhancer(userRepo);
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyPair keyPair = new KeyStoreKeyFactory(
                loadKeyStoreResource(), storepass.toCharArray())
                .getKeyPair(keyname);
        converter.setKeyPair(keyPair);
        return converter;
    }

    private Resource loadKeyStoreResource() {
        if(!StringUtils.isEmpty(this.keystorePath)) {
            log.debug("Load JWT Keystore from File System: {}", this.keystorePath);
            return new FileSystemResource(this.keystorePath);
        } else if(!StringUtils.isEmpty(this.keystoreResource)) {
            log.debug("Load JWT Keystore from Classpath: {}", this.keystoreResource);
            return new ClassPathResource(this.keystoreResource);
        } else {
            throw new BeanCreationException("Could not load keystore for JwtAccessTokenConverter, neither keystore path nor resource path set!");
        }
    }

    @Bean
    public JdbcClientDetailsService getJdbcClientDetailsService() {
        return new JdbcClientDetailsService(dataSource);
    }

}
