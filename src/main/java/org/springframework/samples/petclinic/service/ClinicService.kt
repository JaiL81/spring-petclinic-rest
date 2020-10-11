/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.samples.petclinic.service

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.*

/**
 * Mostly used as a facade so all controllers have a single point of entry
 *
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
interface ClinicService {
    @Throws(DataAccessException::class)
    fun findPetById(id: Int): Pet?

    @Throws(DataAccessException::class)
    fun findAllPets(): Collection<Pet?>?

    @Throws(DataAccessException::class)
    fun savePet(pet: Pet)

    @Throws(DataAccessException::class)
    fun deletePet(pet: Pet)
    fun findVisitsByPetId(petId: Int): Collection<Visit?>?

    @Throws(DataAccessException::class)
    fun findVisitById(visitId: Int): Visit?

    @Throws(DataAccessException::class)
    fun findAllVisits(): Collection<Visit?>?

    @Throws(DataAccessException::class)
    fun saveVisit(visit: Visit)

    @Throws(DataAccessException::class)
    fun deleteVisit(visit: Visit)

    @Throws(DataAccessException::class)
    fun findVetById(id: Int): Vet?

    @Throws(DataAccessException::class)
    fun findVets(): Collection<Vet?>?

    @Throws(DataAccessException::class)
    fun findAllVets(): Collection<Vet?>?

    @Throws(DataAccessException::class)
    fun saveVet(vet: Vet)

    @Throws(DataAccessException::class)
    fun deleteVet(vet: Vet)

    @Throws(DataAccessException::class)
    fun findOwnerById(id: Int): Owner?

    @Throws(DataAccessException::class)
    fun findAllOwners(): Collection<Owner?>?

    @Throws(DataAccessException::class)
    fun saveOwner(owner: Owner)

    @Throws(DataAccessException::class)
    fun deleteOwner(owner: Owner)

    @Throws(DataAccessException::class)
    fun findOwnerByLastName(lastName: String): Collection<Owner?>?
    fun findPetTypeById(petTypeId: Int): PetType?

    @Throws(DataAccessException::class)
    fun findAllPetTypes(): Collection<PetType?>?

    @Throws(DataAccessException::class)
    fun findPetTypes(): Collection<PetType?>?

    @Throws(DataAccessException::class)
    fun savePetType(petType: PetType)

    @Throws(DataAccessException::class)
    fun deletePetType(petType: PetType)
    fun findSpecialtyById(specialtyId: Int): Specialty?

    @Throws(DataAccessException::class)
    fun findAllSpecialties(): Collection<Specialty?>?

    @Throws(DataAccessException::class)
    fun saveSpecialty(specialty: Specialty)

    @Throws(DataAccessException::class)
    fun deleteSpecialty(specialty: Specialty)
}
