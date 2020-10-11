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
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

/**
 * Test class for [OwnerRestController]
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = ApplicationTestConfig::class)
@WebAppConfiguration
class OwnerRestControllerTests {
    @Autowired
    private val ownerRestController: OwnerRestController? = null

    @MockBean
    private val clinicService: ClinicService? = null
    private var mockMvc: MockMvc? = null
    private var owners: MutableList<Owner>? = null

    @Before
    fun initOwners() {
        mockMvc = MockMvcBuilders.standaloneSetup(ownerRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()
        owners = ArrayList()
        val ownerWithPet = Owner()
        ownerWithPet.id = 1
        ownerWithPet.firstName = "George"
        ownerWithPet.lastName = "Franklin"
        ownerWithPet.address = "110 W. Liberty St."
        ownerWithPet.city = "Madison"
        ownerWithPet.telephone = "6085551023"
        ownerWithPet.addPet(getTestPetWithIdAndName(ownerWithPet, 1, "Rosy"))
        owners.add(ownerWithPet)
        var owner = Owner()
        owner.id = 2
        owner.firstName = "Betty"
        owner.lastName = "Davis"
        owner.address = "638 Cardinal Ave."
        owner.city = "Sun Prairie"
        owner.telephone = "6085551749"
        owners.add(owner)
        owner = Owner()
        owner.id = 3
        owner.firstName = "Eduardo"
        owner.lastName = "Rodriquez"
        owner.address = "2693 Commerce St."
        owner.city = "McFarland"
        owner.telephone = "6085558763"
        owners.add(owner)
        owner = Owner()
        owner.id = 4
        owner.firstName = "Harold"
        owner.lastName = "Davis"
        owner.address = "563 Friendly St."
        owner.city = "Windsor"
        owner.telephone = "6085553198"
        owners.add(owner)
    }

    private fun getTestPetWithIdAndName(owner: Owner, id: Int, name: String): Pet {
        val petType = PetType()
        petType.id = 2
        petType.name = "dog"
        val pet = Pet()
        pet.id = id
        pet.name = name
        pet.birthDate = Date()
        pet.owner = owner
        pet.type = petType
        pet.addVisit(getTestVisitForPet(pet, 1))
        return pet
    }

    private fun getTestVisitForPet(pet: Pet, id: Int): Visit {
        val visit = Visit()
        visit.id = id
        visit.pet = pet
        visit.date = Date()
        visit.description = "test$id"
        return visit
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetOwnerSuccess() {
        given(clinicService!!.findOwnerById(1)).willReturn(owners!![0])
        mockMvc.perform(get("/api/owners/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("George"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetOwnerNotFound() {
        given(clinicService!!.findOwnerById(-1)).willReturn(null)
        mockMvc.perform(get("/api/owners/-1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetOwnersListSuccess() {
        owners!!.removeAt(0)
        owners!!.removeAt(1)
        given(clinicService!!.findOwnerByLastName("Davis")).willReturn(owners)
        mockMvc.perform(get("/api/owners/*/lastname/Davis")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.[0].id").value(2))
                .andExpect(jsonPath("$.[0].firstName").value("Betty"))
                .andExpect(jsonPath("$.[1].id").value(4))
                .andExpect(jsonPath("$.[1].firstName").value("Harold"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetOwnersListNotFound() {
        owners!!.clear()
        given(clinicService!!.findOwnerByLastName("0")).willReturn(owners)
        mockMvc.perform(get("/api/owners/?lastName=0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetAllOwnersSuccess() {
        owners!!.removeAt(0)
        owners!!.removeAt(1)
        given(clinicService!!.findAllOwners()).willReturn(owners)
        mockMvc.perform(get("/api/owners/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.[0].id").value(2))
                .andExpect(jsonPath("$.[0].firstName").value("Betty"))
                .andExpect(jsonPath("$.[1].id").value(4))
                .andExpect(jsonPath("$.[1].firstName").value("Harold"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetAllOwnersNotFound() {
        owners!!.clear()
        given(clinicService!!.findAllOwners()).willReturn(owners)
        mockMvc.perform(get("/api/owners/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testCreateOwnerSuccess() {
        val newOwner = owners!![0]
        newOwner.id = null
        val mapper = ObjectMapper()
        val newOwnerAsJSON: String = mapper.writeValueAsString(newOwner)
        mockMvc.perform(post("/api/owners/")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testCreateOwnerErrorIdSpecified() {
        val newOwner = owners!![0]
        newOwner.id = 999
        val mapper = ObjectMapper()
        val newOwnerAsJSON: String = mapper.writeValueAsString(newOwner)
        mockMvc.perform(post("/api/owners/")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errors",
                        "[{\"objectName\":\"body\",\"fieldName\":\"id\",\"fieldValue\":\"999\",\"errorMessage\":\"must not be specified\"}]"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testCreateOwnerError() {
        val newOwner = owners!![0]
        newOwner.id = null
        newOwner.firstName = null
        val mapper = ObjectMapper()
        val newOwnerAsJSON: String = mapper.writeValueAsString(newOwner)
        mockMvc.perform(post("/api/owners/")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testUpdateOwnerSuccess() {
        given(clinicService!!.findOwnerById(1)).willReturn(owners!![0])
        val ownerId = owners!![0].id!!
        val updatedOwner = Owner()
        // body.id = ownerId which is used in url path
        updatedOwner.id = ownerId
        updatedOwner.firstName = "George I"
        updatedOwner.lastName = "Franklin"
        updatedOwner.address = "110 W. Liberty St."
        updatedOwner.city = "Madison"
        updatedOwner.telephone = "6085551023"
        val mapper = ObjectMapper()
        val newOwnerAsJSON: String = mapper.writeValueAsString(updatedOwner)
        mockMvc.perform(put("/api/owners/$ownerId")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isNoContent())
        mockMvc.perform(get("/api/owners/$ownerId")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(ownerId))
                .andExpect(jsonPath("$.firstName").value("George I"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testUpdateOwnerSuccessNoBodyId() {
        given(clinicService!!.findOwnerById(1)).willReturn(owners!![0])
        val ownerId = owners!![0].id!!
        val updatedOwner = Owner()
        updatedOwner.firstName = "George I"
        updatedOwner.lastName = "Franklin"
        updatedOwner.address = "110 W. Liberty St."
        updatedOwner.city = "Madison"
        updatedOwner.telephone = "6085551023"
        val mapper = ObjectMapper()
        val newOwnerAsJSON: String = mapper.writeValueAsString(updatedOwner)
        mockMvc.perform(put("/api/owners/$ownerId")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isNoContent())
        mockMvc.perform(get("/api/owners/$ownerId")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(ownerId))
                .andExpect(jsonPath("$.firstName").value("George I"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testUpdateOwnerErrorBodyIdMismatchWithPathId() {
        val ownerId = owners!![0].id!!
        val updatedOwner = Owner()
        // body.id != ownerId
        updatedOwner.id = -1
        updatedOwner.firstName = "George I"
        updatedOwner.lastName = "Franklin"
        updatedOwner.address = "110 W. Liberty St."
        updatedOwner.city = "Madison"
        updatedOwner.telephone = "6085551023"
        val mapper = ObjectMapper()
        val newOwnerAsJSON: String = mapper.writeValueAsString(updatedOwner)
        mockMvc.perform(put("/api/owners/$ownerId")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errors",
                        "[{\"objectName\":\"body\",\"fieldName\":\"id\",\"fieldValue\":\"-1\",\"errorMessage\":\"does not match pathId: 1\"}]"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testUpdateOwnerError() {
        val newOwner = owners!![0]
        newOwner.firstName = ""
        val mapper = ObjectMapper()
        val newOwnerAsJSON: String = mapper.writeValueAsString(newOwner)
        mockMvc.perform(put("/api/owners/1")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testDeleteOwnerSuccess() {
        val newOwner = owners!![0]
        val mapper = ObjectMapper()
        val newOwnerAsJSON: String = mapper.writeValueAsString(newOwner)
        given(clinicService!!.findOwnerById(1)).willReturn(owners!![0])
        mockMvc.perform(delete("/api/owners/1")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testDeleteOwnerError() {
        val newOwner = owners!![0]
        val mapper = ObjectMapper()
        val newOwnerAsJSON: String = mapper.writeValueAsString(newOwner)
        given(clinicService!!.findOwnerById(-1)).willReturn(null)
        mockMvc.perform(delete("/api/owners/-1")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
    }
}
