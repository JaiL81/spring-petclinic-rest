package org.springframework.samples.petclinic.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
 * Starting from Spring Boot 2, if Spring Security is present, endpoints are secured by default
 * using Spring Security’s content-negotiation strategy.
 */
@Configuration
@ConditionalOnProperty(name = "petclinic.security.enable", havingValue = "false")
class DisableSecurityConfig : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    protected fun configure(http: HttpSecurity) {
        // @formatter:off
        http
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .csrf()
                .disable()
        // @formatter:on
    }
}
