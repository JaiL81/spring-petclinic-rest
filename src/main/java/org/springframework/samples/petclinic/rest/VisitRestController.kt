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
import org.springframework.samples.petclinic.model.Visit
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
@RequestMapping("api/visits")
class VisitRestController {
    @Autowired
    private val clinicService: ClinicService? = null

    @get:RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @get:PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    val allVisits: ResponseEntity<Collection<Visit>>
        get() {
            val visits: MutableCollection<Visit> = ArrayList()
            visits.addAll(clinicService!!.findAllVisits())
            return if (visits.isEmpty()) {
                ResponseEntity<Collection<Visit>>(HttpStatus.NOT_FOUND)
            } else ResponseEntity<Collection<Visit>>(visits, HttpStatus.OK)
        }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "/{visitId}", method = RequestMethod.GET, produces = "application/json")
    fun getVisit(@PathVariable("visitId") visitId: Int): ResponseEntity<Visit> {
        val visit = clinicService!!.findVisitById(visitId)
                ?: return ResponseEntity<Visit>(HttpStatus.NOT_FOUND)
        return ResponseEntity<Visit>(visit, HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    fun addVisit(@RequestBody @Valid visit: Visit?, bindingResult: BindingResult, ucBuilder: UriComponentsBuilder): ResponseEntity<Visit> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || visit == null || visit.pet == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<Visit>(headers, HttpStatus.BAD_REQUEST)
        }
        clinicService!!.saveVisit(visit)
        headers.setLocation(ucBuilder.path("/api/visits/{id}").buildAndExpand(visit.id).toUri())
        return ResponseEntity<Visit>(visit, headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "/{visitId}", method = RequestMethod.PUT, produces = "application/json")
    fun updateVisit(@PathVariable("visitId") visitId: Int, @RequestBody @Valid visit: Visit?, bindingResult: BindingResult): ResponseEntity<Visit> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || visit == null || visit.pet == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<Visit>(headers, HttpStatus.BAD_REQUEST)
        }
        val currentVisit = clinicService!!.findVisitById(visitId)
                ?: return ResponseEntity<Visit>(HttpStatus.NOT_FOUND)
        currentVisit.date = visit.date
        currentVisit.description = visit.description
        currentVisit.pet = visit.pet
        clinicService.saveVisit(currentVisit)
        return ResponseEntity<Visit>(currentVisit, HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "/{visitId}", method = RequestMethod.DELETE, produces = "application/json")
    @Transactional
    fun deleteVisit(@PathVariable("visitId") visitId: Int): ResponseEntity<Void> {
        val visit = clinicService!!.findVisitById(visitId)
                ?: return ResponseEntity<Void>(HttpStatus.NOT_FOUND)
        clinicService.deleteVisit(visit)
        return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    }
}
