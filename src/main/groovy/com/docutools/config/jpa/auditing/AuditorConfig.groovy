package com.docutools.config.jpa.auditing

import com.docutools.users.SessionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing(dateTimeProviderRef = 'dateTimeProvider')
@Configuration
class AuditorConfig {

    @Autowired SessionManager sessionManager

    @Bean
    DateTimeProvider dateTimeProvider() {
        new UtcDateTimeProvider()
    }

}