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
import org.springframework.samples.petclinic.model.Specialty
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
 * Test class for [SpecialtyRestController]
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = ApplicationTestConfig::class)
@WebAppConfiguration
class SpecialtyRestControllerTests {
    @Autowired
    private val specialtyRestController: SpecialtyRestController? = null

    @MockBean
    private val clinicService: ClinicService? = null
    private var mockMvc: MockMvc? = null
    private var specialties: MutableList<Specialty>? = null

    @Before
    fun initSpecialtys() {
        mockMvc = MockMvcBuilders.standaloneSetup(specialtyRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()
        specialties = ArrayList()
        var specialty = Specialty()
        specialty.id = 1
        specialty.name = "radiology"
        specialties.add(specialty)
        specialty = Specialty()
        specialty.id = 2
        specialty.name = "surgery"
        specialties.add(specialty)
        specialty = Specialty()
        specialty.id = 3
        specialty.name = "dentistry"
        specialties.add(specialty)
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetSpecialtySuccess() {
        given(clinicService!!.findSpecialtyById(1)).willReturn(specialties!![0])
        mockMvc.perform(get("/api/specialties/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("radiology"))
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetSpecialtyNotFound() {
        given(clinicService!!.findSpecialtyById(-1)).willReturn(null)
        mockMvc.perform(get("/api/specialties/-1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetAllSpecialtysSuccess() {
        specialties!!.removeAt(0)
        given(clinicService!!.findAllSpecialties()).willReturn(specialties)
        mockMvc.perform(get("/api/specialties/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.[0].id").value(2))
                .andExpect(jsonPath("$.[0].name").value("surgery"))
                .andExpect(jsonPath("$.[1].id").value(3))
                .andExpect(jsonPath("$.[1].name").value("dentistry"))
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testGetAllSpecialtysNotFound() {
        specialties!!.clear()
        given(clinicService!!.findAllSpecialties()).willReturn(specialties)
        mockMvc.perform(get("/api/specialties/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testCreateSpecialtySuccess() {
        val newSpecialty = specialties!![0]
        newSpecialty.id = 999
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON: String = mapper.writeValueAsString(newSpecialty)
        mockMvc.perform(post("/api/specialties/")
                .content(newSpecialtyAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testCreateSpecialtyError() {
        val newSpecialty = specialties!![0]
        newSpecialty.id = null
        newSpecialty.name = null
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON: String = mapper.writeValueAsString(newSpecialty)
        mockMvc.perform(post("/api/specialties/")
                .content(newSpecialtyAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testUpdateSpecialtySuccess() {
        given(clinicService!!.findSpecialtyById(2)).willReturn(specialties!![1])
        val newSpecialty = specialties!![1]
        newSpecialty.name = "surgery I"
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON: String = mapper.writeValueAsString(newSpecialty)
        mockMvc.perform(put("/api/specialties/2")
                .content(newSpecialtyAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isNoContent())
        mockMvc.perform(get("/api/specialties/2")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("surgery I"))
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testUpdateSpecialtyError() {
        val newSpecialty = specialties!![0]
        newSpecialty.name = ""
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON: String = mapper.writeValueAsString(newSpecialty)
        mockMvc.perform(put("/api/specialties/1")
                .content(newSpecialtyAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testDeleteSpecialtySuccess() {
        val newSpecialty = specialties!![0]
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON: String = mapper.writeValueAsString(newSpecialty)
        given(clinicService!!.findSpecialtyById(1)).willReturn(specialties!![0])
        mockMvc.perform(delete("/api/specialties/1")
                .content(newSpecialtyAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @Throws(Exception::class)
    fun testDeleteSpecialtyError() {
        val newSpecialty = specialties!![0]
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON: String = mapper.writeValueAsString(newSpecialty)
        given(clinicService!!.findSpecialtyById(-1)).willReturn(null)
        mockMvc.perform(delete("/api/specialties/-1")
                .content(newSpecialtyAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
    }
}
