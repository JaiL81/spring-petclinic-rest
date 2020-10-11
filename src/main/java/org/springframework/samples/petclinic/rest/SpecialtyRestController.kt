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
import org.springframework.samples.petclinic.model.Specialty
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

/**
 * @author Vitaliy Fedoriv
 */
@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api/specialties")
class SpecialtyRestController {
    @Autowired
    private val clinicService: ClinicService? = null

    @get:RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @get:PreAuthorize("hasRole(@roles.VET_ADMIN)")
    val allSpecialtys: ResponseEntity<Collection<Specialty>>
        get() {
            val specialties: MutableCollection<Specialty> = ArrayList()
            specialties.addAll(clinicService!!.findAllSpecialties())
            return if (specialties.isEmpty()) {
                ResponseEntity<Collection<Specialty>>(HttpStatus.NOT_FOUND)
            } else ResponseEntity<Collection<Specialty>>(specialties, HttpStatus.OK)
        }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "/{specialtyId}", method = RequestMethod.GET, produces = "application/json")
    fun getSpecialty(@PathVariable("specialtyId") specialtyId: Int): ResponseEntity<Specialty> {
        val specialty = clinicService!!.findSpecialtyById(specialtyId)
                ?: return ResponseEntity<Specialty>(HttpStatus.NOT_FOUND)
        return ResponseEntity<Specialty>(specialty, HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    fun addSpecialty(@RequestBody @Valid specialty: Specialty?, bindingResult: BindingResult, ucBuilder: UriComponentsBuilder): ResponseEntity<Specialty> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || specialty == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<Specialty>(headers, HttpStatus.BAD_REQUEST)
        }
        clinicService!!.saveSpecialty(specialty)
        headers.setLocation(ucBuilder.path("/api/specialtys/{id}").buildAndExpand(specialty.id).toUri())
        return ResponseEntity<Specialty>(specialty, headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "/{specialtyId}", method = RequestMethod.PUT, produces = "application/json")
    fun updateSpecialty(@PathVariable("specialtyId") specialtyId: Int, @RequestBody @Valid specialty: Specialty?, bindingResult: BindingResult): ResponseEntity<Specialty> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || specialty == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<Specialty>(headers, HttpStatus.BAD_REQUEST)
        }
        val currentSpecialty = clinicService!!.findSpecialtyById(specialtyId)
                ?: return ResponseEntity<Specialty>(HttpStatus.NOT_FOUND)
        currentSpecialty.name = specialty.name
        clinicService.saveSpecialty(currentSpecialty)
        return ResponseEntity<Specialty>(currentSpecialty, HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "/{specialtyId}", method = RequestMethod.DELETE, produces = "application/json")
    @Transactional
    fun deleteSpecialty(@PathVariable("specialtyId") specialtyId: Int): ResponseEntity<Void> {
        val specialty = clinicService!!.findSpecialtyById(specialtyId)
                ?: return ResponseEntity<Void>(HttpStatus.NOT_FOUND)
        clinicService.deleteSpecialty(specialty)
        return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    }
}
