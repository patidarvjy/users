package com.docutools.config.api;

import org.springframework.security.core.context.SecurityContextHolder;

public class BaseController {

    protected static String getUserName() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

}
