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
import org.springframework.samples.petclinic.model.Vet
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
 * Test class for [VetRestController]
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = ApplicationTestConfig::class)
@WebAppConfiguration
class VetRestControllerTests {
    @Autowired
    private val vetRestController: VetRestController? = null

    @MockBean
    private val clinicService: ClinicService? = null
    private var mockMvc: MockMvc? = null
    private var vets: MutableList<Vet>? = null

    @Before
    fun initVets() {
        mockMvc = MockMvcBuilders.standaloneSetup(vetRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()
        vets = ArrayList()
        var vet = Vet()
        vet.id = 1
        vet.firstName = "James"
        vet.lastName = "Carter"
        vets.add(vet)
        vet = Vet()
        vet.id = 2
        vet.firstName = "Helen"
        vet.lastName = "Leary"
        vets.add(vet)
        vet = Vet()
        vet.id = 3
        vet.firstName = "Linda"
        vet.lastName = "Douglas"
        vets.add(vet)
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetVetSuccess() {
        given(clinicService!!.findVetById(1)).willReturn(vets!![0])
        mockMvc.perform(get("/api/vets/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("James"))
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetVetNotFound() {
        given(clinicService!!.findVetById(-1)).willReturn(null)
        mockMvc.perform(get("/api/vets/-1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetAllVetsSuccess() {
        given(clinicService!!.findAllVets()).willReturn(vets)
        mockMvc.perform(get("/api/vets/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].firstName").value("James"))
                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].firstName").value("Helen"))
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetAllVetsNotFound() {
        vets!!.clear()
        given(clinicService!!.findAllVets()).willReturn(vets)
        mockMvc.perform(get("/api/vets/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testCreateVetSuccess() {
        val newVet = vets!![0]
        newVet.id = 999
        val mapper = ObjectMapper()
        val newVetAsJSON: String = mapper.writeValueAsString(newVet)
        mockMvc.perform(post("/api/vets/")
                .content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testCreateVetError() {
        val newVet = vets!![0]
        newVet.id = null
        newVet.firstName = null
        val mapper = ObjectMapper()
        val newVetAsJSON: String = mapper.writeValueAsString(newVet)
        mockMvc.perform(post("/api/vets/")
                .content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testUpdateVetSuccess() {
        given(clinicService!!.findVetById(1)).willReturn(vets!![0])
        val newVet = vets!![0]
        newVet.firstName = "James"
        val mapper = ObjectMapper()
        val newVetAsJSON: String = mapper.writeValueAsString(newVet)
        mockMvc.perform(put("/api/vets/1")
                .content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isNoContent())
        mockMvc.perform(get("/api/vets/1")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("James"))
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testUpdateVetError() {
        val newVet = vets!![0]
        newVet.firstName = ""
        val mapper = ObjectMapper()
        val newVetAsJSON: String = mapper.writeValueAsString(newVet)
        mockMvc.perform(put("/api/vets/1")
                .content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testDeleteVetSuccess() {
        val newVet = vets!![0]
        val mapper = ObjectMapper()
        val newVetAsJSON: String = mapper.writeValueAsString(newVet)
        given(clinicService!!.findVetById(1)).willReturn(vets!![0])
        mockMvc.perform(delete("/api/vets/1")
                .content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testDeleteVetError() {
        val newVet = vets!![0]
        val mapper = ObjectMapper()
        val newVetAsJSON: String = mapper.writeValueAsString(newVet)
        given(clinicService!!.findVetById(-1)).willReturn(null)
        mockMvc.perform(delete("/api/vets/-1")
                .content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
    }
}
