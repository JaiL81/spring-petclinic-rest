package org.springframework.samples.petclinic.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.samples.petclinic.model.User
import org.springframework.samples.petclinic.service.UserService
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = ApplicationTestConfig::class)
@WebAppConfiguration
class UserRestControllerTests {
    @Mock
    private val userService: UserService? = null

    @Autowired
    private val userRestController: UserRestController? = null
    private var mockMvc: MockMvc? = null

    @Before
    fun initVets() {
        mockMvc = MockMvcBuilders.standaloneSetup(userRestController)
                .setControllerAdvice(ExceptionControllerAdvice()).build()
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Throws(Exception::class)
    fun testCreateUserSuccess() {
        val user = User()
        user.username = "username"
        user.password = "password"
        user.enabled = true
        user.addRole("OWNER_ADMIN")
        val mapper = ObjectMapper()
        val newVetAsJSON: String = mapper.writeValueAsString(user)
        mockMvc.perform(post("/api/users/")
                .content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Throws(Exception::class)
    fun testCreateUserError() {
        val user = User()
        user.username = "username"
        user.password = "password"
        user.enabled = true
        val mapper = ObjectMapper()
        val newVetAsJSON: String = mapper.writeValueAsString(user)
        mockMvc.perform(post("/api/users/")
                .content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
    }
}
