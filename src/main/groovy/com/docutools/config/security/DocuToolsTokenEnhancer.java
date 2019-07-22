package com.docutools.config.security;

import com.docutools.users.DocutoolsUser;
import com.docutools.users.UserRepo;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link TokenEnhancer} adding the following additional info to the JWT:
 * <ul>
 *     <li>First and Last names</li>
 *     <li>Email address</li>
 *     <li>User ID and Organization ID</li>
 * </ul>
 */
public class DocuToolsTokenEnhancer implements TokenEnhancer {

    private static final Logger log = LoggerFactory.getLogger(DocuToolsTokenEnhancer.class);

    private final LoadingCache<String, Map<String,Object>> additionalInfoCache;

    DocuToolsTokenEnhancer(UserRepo userRepo) {
        this.additionalInfoCache = CacheBuilder.newBuilder()
                .maximumSize(250)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> load(@NotNull String key) throws Exception {
                        return userRepo.findByUsernameIgnoreCase(key)
                                .map(DocuToolsTokenEnhancer::createAdditionalInfo)
                                .orElse(new HashMap<>());
                    }
                });
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        try {
            extractUserNameFrom(authentication)
                    .map(additionalInfoCache::getUnchecked)
                    .ifPresent(((DefaultOAuth2AccessToken) accessToken)::setAdditionalInformation);
        } catch (Exception e) {
            log.warn("Could not load Additional JWT Fields.", e);
        }
        return accessToken;
    }

    private Optional<String> extractUserNameFrom(OAuth2Authentication authentication) {
        return Optional.ofNullable(authentication.getUserAuthentication())
                .map(Authentication::getDetails)
                .filter(details -> details instanceof LinkedHashMap)
                .map(details -> (LinkedHashMap)details)
                .map(details -> details.get("username"))
                .filter(username -> username instanceof String)
                .map(username -> (String) username);
    }


    static Map<String, Object> createAdditionalInfo(DocutoolsUser user) {
        Map<String,Object> additionalInfo = new HashMap<>();
        additionalInfo.put("id", user.getId());
        additionalInfo.put("organizationId", user.getOrganisation().getId());
        additionalInfo.put("organizationName", user.getOrganisation().getName() == null ?
                "" : user.getOrganisation().getName());
        additionalInfo.put("email", user.getEmail());
        if(user.getName() != null) {
            additionalInfo.put("firstName", user.getName().getFirstName());
            additionalInfo.put("lastName", user.getName().getLastName());
        }
        return additionalInfo;
    }

}
