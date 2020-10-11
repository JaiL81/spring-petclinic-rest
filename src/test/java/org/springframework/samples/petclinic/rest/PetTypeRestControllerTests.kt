/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

/**
 * Test class for [PetTypeRestController]
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = ApplicationTestConfig::class)
@WebAppConfiguration
class PetTypeRestControllerTests {
    @Autowired
    private val petTypeRestController: PetTypeRestController? = null

    @MockBean
    private val clinicService: ClinicService? = null
    private var mockMvc: MockMvc? = null
    private var petTypes: MutableList<PetType>? = null

    @Before
    fun initPetTypes() {
        mockMvc = MockMvcBuilders.standaloneSetup(petTypeRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()
        petTypes = ArrayList()
        var petType = PetType()
        petType.id = 1
        petType.name = "cat"
        petTypes.add(petType)
        petType = PetType()
        petType.id = 2
        petType.name = "dog"
        petTypes.add(petType)
        petType = PetType()
        petType.id = 3
        petType.name = "lizard"
        petTypes.add(petType)
        petType = PetType()
        petType.id = 4
        petType.name = "snake"
        petTypes.add(petType)
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetPetTypeSuccessAsOwnerAdmin() {
        given(clinicService!!.findPetTypeById(1)).willReturn(petTypes!![0])
        mockMvc.perform(get("/api/pettypes/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("cat"))
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetPetTypeSuccessAsVetAdmin() {
        given(clinicService!!.findPetTypeById(1)).willReturn(petTypes!![0])
        mockMvc.perform(get("/api/pettypes/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("cat"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetPetTypeNotFound() {
        given(clinicService!!.findPetTypeById(-1)).willReturn(null)
        mockMvc.perform(get("/api/pettypes/-1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetAllPetTypesSuccessAsOwnerAdmin() {
        petTypes!!.removeAt(0)
        petTypes!!.removeAt(1)
        given(clinicService!!.findAllPetTypes()).willReturn(petTypes)
        mockMvc.perform(get("/api/pettypes/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.[0].id").value(2))
                .andExpect(jsonPath("$.[0].name").value("dog"))
                .andExpect(jsonPath("$.[1].id").value(4))
                .andExpect(jsonPath("$.[1].name").value("snake"))
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetAllPetTypesSuccessAsVetAdmin() {
        petTypes!!.removeAt(0)
        petTypes!!.removeAt(1)
        given(clinicService!!.findAllPetTypes()).willReturn(petTypes)
        mockMvc.perform(get("/api/pettypes/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.[0].id").value(2))
                .andExpect(jsonPath("$.[0].name").value("dog"))
                .andExpect(jsonPath("$.[1].id").value(4))
                .andExpect(jsonPath("$.[1].name").value("snake"))
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetAllPetTypesNotFound() {
        petTypes!!.clear()
        given(clinicService!!.findAllPetTypes()).willReturn(petTypes)
        mockMvc.perform(get("/api/pettypes/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testCreatePetTypeSuccess() {
        val newPetType = petTypes!![0]
        newPetType.id = 999
        val mapper = ObjectMapper()
        val newPetTypeAsJSON: String = mapper.writeValueAsString(newPetType)
        mockMvc.perform(post("/api/pettypes/")
                .content(newPetTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testCreatePetTypeError() {
        val newPetType = petTypes!![0]
        newPetType.id = null
        newPetType.name = null
        val mapper = ObjectMapper()
        val newPetTypeAsJSON: String = mapper.writeValueAsString(newPetType)
        mockMvc.perform(post("/api/pettypes/")
                .content(newPetTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testUpdatePetTypeSuccess() {
        given(clinicService!!.findPetTypeById(2)).willReturn(petTypes!![1])
        val newPetType = petTypes!![1]
        newPetType.name = "dog I"
        val mapper = ObjectMapper()
        val newPetTypeAsJSON: String = mapper.writeValueAsString(newPetType)
        mockMvc.perform(put("/api/pettypes/2")
                .content(newPetTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isNoContent())
        mockMvc.perform(get("/api/pettypes/2")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("dog I"))
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testUpdatePetTypeError() {
        val newPetType = petTypes!![0]
        newPetType.name = ""
        val mapper = ObjectMapper()
        val newPetTypeAsJSON: String = mapper.writeValueAsString(newPetType)
        mockMvc.perform(put("/api/pettypes/1")
                .content(newPetTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testDeletePetTypeSuccess() {
        val newPetType = petTypes!![0]
        val mapper = ObjectMapper()
        val newPetTypeAsJSON: String = mapper.writeValueAsString(newPetType)
        given(clinicService!!.findPetTypeById(1)).willReturn(petTypes!![0])
        mockMvc.perform(delete("/api/pettypes/1")
                .content(newPetTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testDeletePetTypeError() {
        val newPetType = petTypes!![0]
        val mapper = ObjectMapper()
        val newPetTypeAsJSON: String = mapper.writeValueAsString(newPetType)
        given(clinicService!!.findPetTypeById(-1)).willReturn(null)
        mockMvc.perform(delete("/api/pettypes/-1")
                .content(newPetTypeAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
    }
}
