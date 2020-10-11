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
import java.util.*
import javax.transaction.Transactional
import javax.validation.Valid

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api/pettypes")
class PetTypeRestController {
    @Autowired
    private val clinicService: ClinicService? = null

    @get:RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @get:PreAuthorize("hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)")
    val allPetTypes: ResponseEntity<Collection<PetType>>
        get() {
            val petTypes: MutableCollection<PetType> = ArrayList()
            petTypes.addAll(clinicService!!.findAllPetTypes())
            return if (petTypes.isEmpty()) {
                ResponseEntity<Collection<PetType>>(HttpStatus.NOT_FOUND)
            } else ResponseEntity<Collection<PetType>>(petTypes, HttpStatus.OK)
        }

    @PreAuthorize("hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)")
    @RequestMapping(value = "/{petTypeId}", method = RequestMethod.GET, produces = "application/json")
    fun getPetType(@PathVariable("petTypeId") petTypeId: Int): ResponseEntity<PetType> {
        val petType = clinicService!!.findPetTypeById(petTypeId)
                ?: return ResponseEntity<PetType>(HttpStatus.NOT_FOUND)
        return ResponseEntity<PetType>(petType, HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    fun addPetType(@RequestBody @Valid petType: PetType?, bindingResult: BindingResult, ucBuilder: UriComponentsBuilder): ResponseEntity<PetType> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || petType == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<PetType>(headers, HttpStatus.BAD_REQUEST)
        }
        clinicService!!.savePetType(petType)
        headers.setLocation(ucBuilder.path("/api/pettypes/{id}").buildAndExpand(petType.id).toUri())
        return ResponseEntity<PetType>(petType, headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "/{petTypeId}", method = RequestMethod.PUT, produces = "application/json")
    fun updatePetType(@PathVariable("petTypeId") petTypeId: Int, @RequestBody @Valid petType: PetType?, bindingResult: BindingResult): ResponseEntity<PetType> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || petType == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<PetType>(headers, HttpStatus.BAD_REQUEST)
        }
        val currentPetType = clinicService!!.findPetTypeById(petTypeId)
                ?: return ResponseEntity<PetType>(HttpStatus.NOT_FOUND)
        currentPetType.name = petType.name
        clinicService.savePetType(currentPetType)
        return ResponseEntity<PetType>(currentPetType, HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "/{petTypeId}", method = RequestMethod.DELETE, produces = "application/json")
    @Transactional
    fun deletePetType(@PathVariable("petTypeId") petTypeId: Int): ResponseEntity<Void> {
        val petType = clinicService!!.findPetTypeById(petTypeId)
                ?: return ResponseEntity<Void>(HttpStatus.NOT_FOUND)
        clinicService.deletePetType(petType)
        return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    }
}
