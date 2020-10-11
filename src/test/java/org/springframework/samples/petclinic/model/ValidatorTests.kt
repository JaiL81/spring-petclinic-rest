package org.springframework.samples.petclinic.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.util.*
import javax.validation.ConstraintViolation
import javax.validation.Validator

/**
 * @author Michael Isvy
 * Simple test to make sure that Bean Validation is working
 * (useful when upgrading to a new version of Hibernate Validator/ Bean Validation)
 */
class ValidatorTests {
    private fun createValidator(): Validator {
        val localValidatorFactoryBean = LocalValidatorFactoryBean()
        localValidatorFactoryBean.afterPropertiesSet()
        return localValidatorFactoryBean
    }

    @Test
    fun shouldNotValidateWhenFirstNameEmpty() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val person = Person()
        person.firstName = ""
        person.lastName = "smith"
        val validator: Validator = createValidator()
        val constraintViolations: Set<ConstraintViolation<Person>> = validator.validate(person)
        assertThat(constraintViolations.size).isEqualTo(1)
        val violation: ConstraintViolation<Person> = constraintViolations.iterator().next()
        assertThat(violation.getPropertyPath().toString()).isEqualTo("firstName")
        assertThat(violation.getMessage()).isEqualTo("must not be empty")
    }
}
