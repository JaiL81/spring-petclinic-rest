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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import javax.transaction.Transactional
import javax.validation.Valid

/**
 * @author Vitaliy Fedoriv
 */
@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api/pets")
class PetRestController {
    @Autowired
    private val clinicService: ClinicService? = null

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "/{petId}", method = RequestMethod.GET, produces = "application/json")
    fun getPet(@PathVariable("petId") petId: Int): ResponseEntity<Pet> {
        val pet = clinicService!!.findPetById(petId)
                ?: return ResponseEntity<Pet>(HttpStatus.NOT_FOUND)
        return ResponseEntity<Pet>(pet, HttpStatus.OK)
    }

    @get:RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @get:PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    val pets: ResponseEntity<Collection<Pet>>
        get() {
            val pets = clinicService!!.findAllPets()
            return if (pets.isEmpty()) {
                ResponseEntity<Collection<Pet>>(HttpStatus.NOT_FOUND)
            } else ResponseEntity<Collection<Pet>>(pets, HttpStatus.OK)
        }

    @get:RequestMapping(value = "/pettypes", method = RequestMethod.GET, produces = "application/json")
    @get:PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    val petTypes: ResponseEntity<Collection<PetType>>
        get() = ResponseEntity<Collection<PetType>>(clinicService!!.findPetTypes(), HttpStatus.OK)

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    fun addPet(@RequestBody @Valid pet: Pet?, bindingResult: BindingResult, ucBuilder: UriComponentsBuilder): ResponseEntity<Pet> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || pet == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<Pet>(headers, HttpStatus.BAD_REQUEST)
        }
        clinicService!!.savePet(pet)
        headers.setLocation(ucBuilder.path("/api/pets/{id}").buildAndExpand(pet.id).toUri())
        return ResponseEntity<Pet>(pet, headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "/{petId}", method = RequestMethod.PUT, produces = "application/json")
    fun updatePet(@PathVariable("petId") petId: Int, @RequestBody @Valid pet: Pet?, bindingResult: BindingResult): ResponseEntity<Pet> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || pet == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<Pet>(headers, HttpStatus.BAD_REQUEST)
        }
        val currentPet = clinicService!!.findPetById(petId)
                ?: return ResponseEntity<Pet>(HttpStatus.NOT_FOUND)
        currentPet.birthDate = pet.birthDate
        currentPet.name = pet.name
        currentPet.type = pet.type
        currentPet.owner = pet.owner
        clinicService.savePet(currentPet)
        return ResponseEntity<Pet>(currentPet, HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "/{petId}", method = RequestMethod.DELETE, produces = "application/json")
    @Transactional
    fun deletePet(@PathVariable("petId") petId: Int): ResponseEntity<Void> {
        val pet = clinicService!!.findPetById(petId)
                ?: return ResponseEntity<Void>(HttpStatus.NOT_FOUND)
        clinicService.deletePet(pet)
        return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    }
}
