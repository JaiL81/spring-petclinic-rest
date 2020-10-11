package org.springframework.samples.petclinic.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import javax.sql.DataSource

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize method-level security
@ConditionalOnProperty(name = "petclinic.security.enable", havingValue = "true")
class BasicAuthenticationConfig : WebSecurityConfigurerAdapter() {
    @Autowired
    private val dataSource: DataSource? = null

    @Throws(Exception::class)
    protected fun configure(http: HttpSecurity) {
        // @formatter:off
        http
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                .and()
                .csrf()
                .disable()
        // @formatter:on
    }

    @Autowired
    @Throws(Exception::class)
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        // @formatter:off
        auth
                .jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("select username,password,enabled from users where username=?")
                .authoritiesByUsernameQuery("select username,role from roles where username=?")
        // @formatter:on
    }
}
