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
import org.springframework.samples.petclinic.model.Owner
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
@RequestMapping("/api/owners")
class OwnerRestController {
    @Autowired
    private val clinicService: ClinicService? = null

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "/*/lastname/{lastName}", method = RequestMethod.GET, produces = "application/json")
    fun getOwnersList(@PathVariable("lastName") ownerLastName: String?): ResponseEntity<Collection<Owner>> {
        var ownerLastName = ownerLastName
        if (ownerLastName == null) {
            ownerLastName = ""
        }
        val owners = clinicService!!.findOwnerByLastName(ownerLastName)
        return if (owners.isEmpty()) {
            ResponseEntity<Collection<Owner>>(HttpStatus.NOT_FOUND)
        } else ResponseEntity<Collection<Owner>>(owners, HttpStatus.OK)
    }

    @get:RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @get:PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    val owners: ResponseEntity<Collection<Owner>>
        get() {
            val owners = clinicService!!.findAllOwners()
            return if (owners.isEmpty()) {
                ResponseEntity<Collection<Owner>>(HttpStatus.NOT_FOUND)
            } else ResponseEntity<Collection<Owner>>(owners, HttpStatus.OK)
        }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "/{ownerId}", method = RequestMethod.GET, produces = "application/json")
    fun getOwner(@PathVariable("ownerId") ownerId: Int): ResponseEntity<Owner> {
        var owner: Owner? = null
        owner = clinicService!!.findOwnerById(ownerId)
        return if (owner == null) {
            ResponseEntity<Owner>(HttpStatus.NOT_FOUND)
        } else ResponseEntity<Owner>(owner, HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    fun addOwner(@RequestBody @Valid owner: Owner, bindingResult: BindingResult,
                 ucBuilder: UriComponentsBuilder): ResponseEntity<Owner> {
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || owner.id != null) {
            val errors = BindingErrorsResponse(owner.id)
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity<Owner>(headers, HttpStatus.BAD_REQUEST)
        }
        clinicService!!.saveOwner(owner)
        headers.setLocation(ucBuilder.path("/api/owners/{id}").buildAndExpand(owner.id).toUri())
        return ResponseEntity<Owner>(owner, headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "/{ownerId}", method = RequestMethod.PUT, produces = "application/json")
    fun updateOwner(@PathVariable("ownerId") ownerId: Int, @RequestBody @Valid owner: Owner,
                    bindingResult: BindingResult, ucBuilder: UriComponentsBuilder?): ResponseEntity<Owner> {
        val bodyIdMatchesPathId = owner.id == null || ownerId == owner.id
        if (bindingResult.hasErrors() || !bodyIdMatchesPathId) {
            val errors = BindingErrorsResponse(ownerId, owner.id)
            errors.addAllErrors(bindingResult)
            val headers = HttpHeaders()
            headers.add("errors", errors.toJSON())
            return ResponseEntity<Owner>(headers, HttpStatus.BAD_REQUEST)
        }
        val currentOwner = clinicService!!.findOwnerById(ownerId)
                ?: return ResponseEntity<Owner>(HttpStatus.NOT_FOUND)
        currentOwner.address = owner.address
        currentOwner.city = owner.city
        currentOwner.firstName = owner.firstName
        currentOwner.lastName = owner.lastName
        currentOwner.telephone = owner.telephone
        clinicService.saveOwner(currentOwner)
        return ResponseEntity<Owner>(currentOwner, HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @RequestMapping(value = "/{ownerId}", method = RequestMethod.DELETE, produces = "application/json")
    @Transactional
    fun deleteOwner(@PathVariable("ownerId") ownerId: Int): ResponseEntity<Void> {
        val owner = clinicService!!.findOwnerById(ownerId)
                ?: return ResponseEntity<Void>(HttpStatus.NOT_FOUND)
        clinicService.deleteOwner(owner)
        return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    }
}
