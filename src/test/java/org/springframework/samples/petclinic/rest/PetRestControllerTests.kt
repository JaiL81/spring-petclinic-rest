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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

/**
 * Test class for [PetRestController]
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = ApplicationTestConfig::class)
@WebAppConfiguration
class PetRestControllerTests {
    @Autowired
    private val petRestController: PetRestController? = null

    @MockBean
    protected var clinicService: ClinicService? = null
    private var mockMvc: MockMvc? = null
    private var pets: MutableList<Pet>? = null

    @Before
    fun initPets() {
        mockMvc = MockMvcBuilders.standaloneSetup(petRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()
        pets = ArrayList()
        val owner = Owner()
        owner.id = 1
        owner.firstName = "Eduardo"
        owner.lastName = "Rodriquez"
        owner.address = "2693 Commerce St."
        owner.city = "McFarland"
        owner.telephone = "6085558763"
        val petType = PetType()
        petType.id = 2
        petType.name = "dog"
        var pet = Pet()
        pet.id = 3
        pet.name = "Rosy"
        pet.birthDate = Date()
        pet.owner = owner
        pet.type = petType
        pets.add(pet)
        pet = Pet()
        pet.id = 4
        pet.name = "Jewel"
        pet.birthDate = Date()
        pet.owner = owner
        pet.type = petType
        pets.add(pet)
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetPetSuccess() {
        given(clinicService!!.findPetById(3)).willReturn(pets!![0])
        mockMvc.perform(get("/api/pets/3")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Rosy"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetPetNotFound() {
        given(clinicService!!.findPetById(-1)).willReturn(null)
        mockMvc.perform(get("/api/pets/-1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetAllPetsSuccess() {
        given(clinicService!!.findAllPets()).willReturn(pets)
        mockMvc.perform(get("/api/pets/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.[0].id").value(3))
                .andExpect(jsonPath("$.[0].name").value("Rosy"))
                .andExpect(jsonPath("$.[1].id").value(4))
                .andExpect(jsonPath("$.[1].name").value("Jewel"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testGetAllPetsNotFound() {
        pets!!.clear()
        given(clinicService!!.findAllPets()).willReturn(pets)
        mockMvc.perform(get("/api/pets/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testCreatePetSuccess() {
        val newPet = pets!![0]
        newPet.id = 999
        val mapper = ObjectMapper()
        val newPetAsJSON: String = mapper.writeValueAsString(newPet)
        mockMvc.perform(post("/api/pets/")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testCreatePetError() {
        val newPet = pets!![0]
        newPet.id = null
        newPet.name = null
        val mapper = ObjectMapper()
        val newPetAsJSON: String = mapper.writeValueAsString(newPet)
        mockMvc.perform(post("/api/pets/")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testUpdatePetSuccess() {
        given(clinicService!!.findPetById(3)).willReturn(pets!![0])
        val newPet = pets!![0]
        newPet.name = "Rosy I"
        val mapper = ObjectMapper()
        val newPetAsJSON: String = mapper.writeValueAsString(newPet)
        mockMvc.perform(put("/api/pets/3")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isNoContent())
        mockMvc.perform(get("/api/pets/3")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Rosy I"))
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testUpdatePetError() {
        val newPet = pets!![0]
        newPet.name = ""
        val mapper = ObjectMapper()
        val newPetAsJSON: String = mapper.writeValueAsString(newPet)
        mockMvc.perform(put("/api/pets/3")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testDeletePetSuccess() {
        val newPet = pets!![0]
        val mapper = ObjectMapper()
        val newPetAsJSON: String = mapper.writeValueAsString(newPet)
        given(clinicService!!.findPetById(3)).willReturn(pets!![0])
        mockMvc.perform(delete("/api/pets/3")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @Throws(Exception::class)
    fun testDeletePetError() {
        val newPet = pets!![0]
        val mapper = ObjectMapper()
        val newPetAsJSON: String = mapper.writeValueAsString(newPet)
        given(clinicService!!.findPetById(-1)).willReturn(null)
        mockMvc.perform(delete("/api/pets/-1")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
    }
}
