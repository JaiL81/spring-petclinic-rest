package org.springframework.samples.petclinic.service.userService

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.samples.petclinic.model.Role
import org.springframework.samples.petclinic.model.User
import org.springframework.samples.petclinic.service.UserService

abstract class AbstractUserServiceTests {
    @Autowired
    private val userService: UserService? = null

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    @Throws(Exception::class)
    fun shouldAddUser() {
        val user = User()
        user.username = "username"
        user.password = "password"
        user.enabled = true
        user.addRole("OWNER_ADMIN")
        userService!!.saveUser(user)
        assertThat(user.getRoles()!!.parallelStream().allMatch { role: Role -> role.name!!.startsWith("ROLE_") }, `is`(true))
        assertThat(user.getRoles()!!.parallelStream().allMatch { role: Role -> role.user != null }, `is`(true))
    }
}
