package com.docutools.config.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomTokenServices extends DefaultTokenServices {

    private TokenStore tokenStore;
    private AuthenticationManager authenticationManager;

    public CustomTokenServices(TokenStore tokenStore) {
        setTokenStore(tokenStore);
    }

    @Transactional(noRollbackFor={InvalidTokenException.class, InvalidGrantException.class})
    public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest) throws AuthenticationException {
        OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(refreshTokenValue);
        if (refreshToken == null) {
            throw new InvalidGrantException("Invalid refresh token: " + refreshTokenValue);
        }

        OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
        if (this.authenticationManager != null && !authentication.isClientOnly()) {
            // The client has already been authenticated, but the user authentication might be old now, so give it a
            // chance to re-authenticate.
            PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken = new PreAuthenticatedAuthenticationToken(authentication.getUserAuthentication(), "", authentication.getAuthorities());
            preAuthenticatedAuthenticationToken.setDetails(createDetails(authentication.getName()));
            Authentication user = preAuthenticatedAuthenticationToken;
            user = authenticationManager.authenticate(user);
            Object details = authentication.getDetails();
            authentication = new OAuth2Authentication(authentication.getOAuth2Request(), user);
            authentication.setDetails(details);
        }

        // clear out any access tokens already associated with the refresh
        // token.
        tokenStore.removeAccessTokenUsingRefreshToken(refreshToken);

        if (isExpired(refreshToken)) {
            tokenStore.removeRefreshToken(refreshToken);
            throw new InvalidTokenException("Invalid refresh token (expired): " + refreshToken);
        }

        authentication = callCreateRefreshedAuthentication(authentication, tokenRequest);

        OAuth2AccessToken accessToken = callCreateAccessToken(authentication, refreshToken);
        tokenStore.storeAccessToken(accessToken, authentication);

        return accessToken;
    }

    private Object createDetails(String username) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("username", username);
        return details;
    }

    @Override
    public void setTokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
        super.setTokenStore(tokenStore);
    }

    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        super.setAuthenticationManager(authenticationManager);
    }

    private OAuth2Authentication callCreateRefreshedAuthentication(OAuth2Authentication authentication, TokenRequest tokenRequest) {
        try {
            Method method = super.getClass().getSuperclass().getDeclaredMethod("createRefreshedAuthentication", OAuth2Authentication.class, TokenRequest.class);
            method.setAccessible(true);
            return (OAuth2Authentication) method.invoke(this, authentication, tokenRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OAuth2AccessToken callCreateAccessToken(OAuth2Authentication authentication, OAuth2RefreshToken refreshToken) {
        try {
            Method method = super.getClass().getSuperclass().getDeclaredMethod("createAccessToken", OAuth2Authentication.class, OAuth2RefreshToken.class);
            method.setAccessible(true);
            return (OAuth2AccessToken) method.invoke(this, authentication, refreshToken);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
