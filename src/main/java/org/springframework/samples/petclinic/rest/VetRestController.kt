/*
 * Copyright 2016-2018 the original author or authors.
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
import org.springframework.samples.petclinic.model.Vet
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
@RequestMapping("api/vets")
class VetRestController {
    @Autowired
    private val clinicService: ClinicService? = null

    @get:RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @get:PreAuthorize("hasRole(@roles.VET_ADMIN)")
    val allVets: ResponseEntity<Collection<Vet>>
        get() {
            val vets: MutableCollection<Vet> = ArrayList()
            vets.addAll(clinicService!!.findAllVets())
            return if (vets.isEmpty()) {
                ResponseEntity<Collection<Vet>>(HttpStatus.NOT_FOUND)
            } else ResponseEntity<Collection<Vet>>(vets, HttpStatus.OK)
        }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "/{vetId}", method = RequestMethod.GET, produces = "application/json")
    fun getVet(@PathVariable("vetId") vetId: Int): ResponseEntity<Vet> {
        val vet = clinicService!!.findVetById(vetId)
                ?: return ResponseEntity<Vet>(HttpStatus.NOT_FOUND)
        return ResponseEntity<Vet>(vet, HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    fun addVet(@RequestBody @Valid vet: Vet?, bindingResult: BindingResult, ucBuilder: UriComponentsBuilder): ResponseEntity<Vet> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || vet == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<Vet>(headers, HttpStatus.BAD_REQUEST)
        }
        clinicService!!.saveVet(vet)
        headers.setLocation(ucBuilder.path("/api/vets/{id}").buildAndExpand(vet.id).toUri())
        return ResponseEntity<Vet>(vet, headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "/{vetId}", method = RequestMethod.PUT, produces = "application/json")
    fun updateVet(@PathVariable("vetId") vetId: Int, @RequestBody @Valid vet: Vet?, bindingResult: BindingResult): ResponseEntity<Vet> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || vet == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<Vet>(headers, HttpStatus.BAD_REQUEST)
        }
        val currentVet = clinicService!!.findVetById(vetId)
                ?: return ResponseEntity<Vet>(HttpStatus.NOT_FOUND)
        currentVet.firstName = vet.firstName
        currentVet.lastName = vet.lastName
        currentVet.clearSpecialties()
        for (spec in vet.specialties) {
            currentVet.addSpecialty(spec)
        }
        clinicService.saveVet(currentVet)
        return ResponseEntity<Vet>(currentVet, HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @RequestMapping(value = "/{vetId}", method = RequestMethod.DELETE, produces = "application/json")
    @Transactional
    fun deleteVet(@PathVariable("vetId") vetId: Int): ResponseEntity<Void> {
        val vet = clinicService!!.findVetById(vetId)
                ?: return ResponseEntity<Void>(HttpStatus.NOT_FOUND)
        clinicService.deleteVet(vet)
        return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    }
}
