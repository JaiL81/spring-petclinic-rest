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
package org.springframework.samples.petclinic.service.clinicService

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.samples.petclinic.model.*
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.samples.petclinic.util.EntityUtils.getById
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 *
 *  Base class for [ClinicService] integration tests.
 *
 * Subclasses should specify Spring context
 * configuration using [@ContextConfiguration][ContextConfiguration] annotation
 *
 *
 * AbstractclinicServiceTests and its subclasses benefit from the following services provided by the Spring
 * TestContext Framework:    * **Spring IoC container caching** which spares us unnecessary set up
 * time between test execution.  * **Dependency Injection** of test fixture instances, meaning that
 * we don't need to perform application context lookups. See the use of [@Autowired][Autowired] on the `[ ][AbstractClinicServiceTests.clinicService]` instance variable, which uses autowiring *by
 * type*.  * **Transaction management**, meaning each test method is executed in its own transaction,
 * which is automatically rolled back by default. Thus, even if tests insert or otherwise change database state, there
 * is no need for a teardown or cleanup script.  *  An [ ApplicationContext][org.springframework.context.ApplicationContext] is also inherited and can be used for explicit bean lookup if necessary.
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
abstract class AbstractClinicServiceTests {
    @Autowired
    protected var clinicService: ClinicService? = null

    @Test
    fun shouldFindOwnersByLastName() {
        var owners = clinicService!!.findOwnerByLastName("Davis")
        assertThat(owners!!.size).isEqualTo(2)
        owners = clinicService!!.findOwnerByLastName("Daviss")
        assertThat(owners!!.isEmpty()).isTrue()
    }

    @Test
    fun shouldFindSingleOwnerWithPet() {
        val owner = clinicService!!.findOwnerById(1)
        assertThat(owner!!.lastName).startsWith("Franklin")
        assertThat(owner!!.getPets().size).isEqualTo(1)
        assertThat(owner!!.getPets()[0].type).isNotNull()
        assertThat(owner!!.getPets()[0].type!!.name).isEqualTo("cat")
    }

    @Test
    @Transactional
    fun shouldInsertOwner() {
        var owners = clinicService!!.findOwnerByLastName("Schultz")
        val found = owners!!.size
        val owner = Owner()
        owner.firstName = "Sam"
        owner.lastName = "Schultz"
        owner.address = "4, Evans Street"
        owner.city = "Wollongong"
        owner.telephone = "4444444444"
        clinicService!!.saveOwner(owner)
        assertThat(owner.id!!.toLong()).isNotEqualTo(0)
        owners = clinicService!!.findOwnerByLastName("Schultz")
        assertThat(owners!!.size).isEqualTo(found + 1)
    }

    @Test
    @Transactional
    fun shouldUpdateOwner() {
        var owner = clinicService!!.findOwnerById(1)
        val oldLastName = owner!!.lastName
        val newLastName = oldLastName + "X"
        owner.lastName = newLastName
        clinicService!!.saveOwner(owner)

        // retrieving new name from database
        owner = clinicService!!.findOwnerById(1)
        assertThat(owner!!.lastName).isEqualTo(newLastName)
    }

    @Test
    fun shouldFindPetWithCorrectId() {
        val pet7 = clinicService!!.findPetById(7)
        assertThat(pet7!!.name).startsWith("Samantha")
        assertThat(pet7!!.owner!!.firstName).isEqualTo("Jean")
    }

    //    @Test
    //    public void shouldFindAllPetTypes() {
    //        Collection<PetType> petTypes = this.clinicService.findPetTypes();
    //
    //        PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1);
    //        assertThat(petType1.getName()).isEqualTo("cat");
    //        PetType petType4 = EntityUtils.getById(petTypes, PetType.class, 4);
    //        assertThat(petType4.getName()).isEqualTo("snake");
    //    }
    @Test
    @Transactional
    fun shouldInsertPetIntoDatabaseAndGenerateId() {
        var owner6 = clinicService!!.findOwnerById(6)
        val found = owner6!!.getPets().size
        val pet = Pet()
        pet.name = "bowser"
        val types = clinicService!!.findPetTypes()
        pet.type = getById<PetType?>(types, PetType::class.java, 2)
        pet.birthDate = Date()
        owner6.addPet(pet)
        assertThat(owner6.getPets().size).isEqualTo(found + 1)
        clinicService!!.savePet(pet)
        clinicService!!.saveOwner(owner6)
        owner6 = clinicService!!.findOwnerById(6)
        assertThat(owner6!!.getPets().size).isEqualTo(found + 1)
        // checks that id has been generated
        assertThat(pet.id).isNotNull()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun shouldUpdatePetName() {
        var pet7 = clinicService!!.findPetById(7)
        val oldName = pet7!!.name
        val newName = oldName + "X"
        pet7.name = newName
        clinicService!!.savePet(pet7)
        pet7 = clinicService!!.findPetById(7)
        assertThat(pet7!!.name).isEqualTo(newName)
    }

    @Test
    fun shouldFindVets() {
        val vets = clinicService!!.findVets()
        val vet = getById<Vet?>(vets, Vet::class.java, 3)
        assertThat(vet!!.lastName).isEqualTo("Douglas")
        assertThat(vet!!.nrOfSpecialties).isEqualTo(2)
        assertThat(vet!!.getSpecialties()[0].name).isEqualTo("dentistry")
        assertThat(vet!!.getSpecialties()[1].name).isEqualTo("surgery")
    }

    @Test
    @Transactional
    fun shouldAddNewVisitForPet() {
        var pet7 = clinicService!!.findPetById(7)
        val found = pet7!!.getVisits().size
        val visit = Visit()
        pet7.addVisit(visit)
        visit.description = "test"
        clinicService!!.saveVisit(visit)
        clinicService!!.savePet(pet7)
        pet7 = clinicService!!.findPetById(7)
        assertThat(pet7!!.getVisits().size).isEqualTo(found + 1)
        assertThat(visit.id).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun shouldFindVisitsByPetId() {
        val visits = clinicService!!.findVisitsByPetId(7)
        assertThat(visits!!.size).isEqualTo(2)
        val visitArr: Array<Visit> = visits!!.toTypedArray()
        assertThat(visitArr[0].pet).isNotNull()
        assertThat(visitArr[0].date).isNotNull()
        assertThat(visitArr[0].pet!!.id).isEqualTo(7)
    }

    @Test
    fun shouldFindAllPets() {
        val pets = clinicService!!.findAllPets()
        val pet1 = getById<Pet?>(pets, Pet::class.java, 1)
        assertThat(pet1!!.name).isEqualTo("Leo")
        val pet3 = getById<Pet?>(pets, Pet::class.java, 3)
        assertThat(pet3!!.name).isEqualTo("Rosy")
    }

    @Test
    @Transactional
    fun shouldDeletePet() {
        var pet = clinicService!!.findPetById(1)
        clinicService!!.deletePet(pet!!)
        pet = try {
            clinicService!!.findPetById(1)
        } catch (e: Exception) {
            null
        }
        assertThat(pet).isNull()
    }

    @Test
    fun shouldFindVisitDyId() {
        val visit = clinicService!!.findVisitById(1)
        assertThat(visit!!.id).isEqualTo(1)
        assertThat(visit!!.pet!!.name).isEqualTo("Samantha")
    }

    @Test
    fun shouldFindAllVisits() {
        val visits = clinicService!!.findAllVisits()
        val visit1 = getById<Visit?>(visits, Visit::class.java, 1)
        assertThat(visit1!!.pet!!.name).isEqualTo("Samantha")
        val visit3 = getById<Visit?>(visits, Visit::class.java, 3)
        assertThat(visit3!!.pet!!.name).isEqualTo("Max")
    }

    @Test
    @Transactional
    fun shouldInsertVisit() {
        var visits = clinicService!!.findAllVisits()
        val found = visits!!.size
        val pet = clinicService!!.findPetById(1)
        val visit = Visit()
        visit.pet = pet
        visit.date = Date()
        visit.description = "new visit"
        clinicService!!.saveVisit(visit)
        assertThat(visit.id!!.toLong()).isNotEqualTo(0)
        visits = clinicService!!.findAllVisits()
        assertThat(visits!!.size).isEqualTo(found + 1)
    }

    @Test
    @Transactional
    fun shouldUpdateVisit() {
        var visit = clinicService!!.findVisitById(1)
        val oldDesc = visit!!.description
        val newDesc = oldDesc + "X"
        visit.description = newDesc
        clinicService!!.saveVisit(visit)
        visit = clinicService!!.findVisitById(1)
        assertThat(visit!!.description).isEqualTo(newDesc)
    }

    @Test
    @Transactional
    fun shouldDeleteVisit() {
        var visit = clinicService!!.findVisitById(1)
        clinicService!!.deleteVisit(visit!!)
        visit = try {
            clinicService!!.findVisitById(1)
        } catch (e: Exception) {
            null
        }
        assertThat(visit).isNull()
    }

    @Test
    fun shouldFindVetDyId() {
        val vet = clinicService!!.findVetById(1)
        assertThat(vet!!.firstName).isEqualTo("James")
        assertThat(vet!!.lastName).isEqualTo("Carter")
    }

    @Test
    @Transactional
    fun shouldInsertVet() {
        var vets = clinicService!!.findAllVets()
        val found = vets!!.size
        val vet = Vet()
        vet.firstName = "John"
        vet.lastName = "Dow"
        clinicService!!.saveVet(vet)
        assertThat(vet.id!!.toLong()).isNotEqualTo(0)
        vets = clinicService!!.findAllVets()
        assertThat(vets!!.size).isEqualTo(found + 1)
    }

    @Test
    @Transactional
    fun shouldUpdateVet() {
        var vet = clinicService!!.findVetById(1)
        val oldLastName = vet!!.lastName
        val newLastName = oldLastName + "X"
        vet.lastName = newLastName
        clinicService!!.saveVet(vet)
        vet = clinicService!!.findVetById(1)
        assertThat(vet!!.lastName).isEqualTo(newLastName)
    }

    @Test
    @Transactional
    fun shouldDeleteVet() {
        var vet = clinicService!!.findVetById(1)
        clinicService!!.deleteVet(vet!!)
        vet = try {
            clinicService!!.findVetById(1)
        } catch (e: Exception) {
            null
        }
        assertThat(vet).isNull()
    }

    @Test
    fun shouldFindAllOwners() {
        val owners = clinicService!!.findAllOwners()
        val owner1 = getById<Owner?>(owners, Owner::class.java, 1)
        assertThat(owner1!!.firstName).isEqualTo("George")
        val owner3 = getById<Owner?>(owners, Owner::class.java, 3)
        assertThat(owner3!!.firstName).isEqualTo("Eduardo")
    }

    @Test
    @Transactional
    fun shouldDeleteOwner() {
        var owner = clinicService!!.findOwnerById(1)
        clinicService!!.deleteOwner(owner!!)
        owner = try {
            clinicService!!.findOwnerById(1)
        } catch (e: Exception) {
            null
        }
        assertThat(owner).isNull()
    }

    @Test
    fun shouldFindPetTypeById() {
        val petType = clinicService!!.findPetTypeById(1)
        assertThat(petType!!.name).isEqualTo("cat")
    }

    @Test
    fun shouldFindAllPetTypes() {
        val petTypes = clinicService!!.findAllPetTypes()
        val petType1 = getById<PetType?>(petTypes, PetType::class.java, 1)
        assertThat(petType1!!.name).isEqualTo("cat")
        val petType3 = getById<PetType?>(petTypes, PetType::class.java, 3)
        assertThat(petType3!!.name).isEqualTo("lizard")
    }

    @Test
    @Transactional
    fun shouldInsertPetType() {
        var petTypes = clinicService!!.findAllPetTypes()
        val found = petTypes!!.size
        val petType = PetType()
        petType.name = "tiger"
        clinicService!!.savePetType(petType)
        assertThat(petType.id!!.toLong()).isNotEqualTo(0)
        petTypes = clinicService!!.findAllPetTypes()
        assertThat(petTypes!!.size).isEqualTo(found + 1)
    }

    @Test
    @Transactional
    fun shouldUpdatePetType() {
        var petType = clinicService!!.findPetTypeById(1)
        val oldLastName = petType!!.name
        val newLastName = oldLastName + "X"
        petType.name = newLastName
        clinicService!!.savePetType(petType)
        petType = clinicService!!.findPetTypeById(1)
        assertThat(petType!!.name).isEqualTo(newLastName)
    }

    @Test
    @Transactional
    fun shouldDeletePetType() {
        var petType = clinicService!!.findPetTypeById(1)
        clinicService!!.deletePetType(petType!!)
        petType = try {
            clinicService!!.findPetTypeById(1)
        } catch (e: Exception) {
            null
        }
        assertThat(petType).isNull()
    }

    @Test
    fun shouldFindSpecialtyById() {
        val specialty = clinicService!!.findSpecialtyById(1)
        assertThat(specialty!!.name).isEqualTo("radiology")
    }

    @Test
    fun shouldFindAllSpecialtys() {
        val specialties = clinicService!!.findAllSpecialties()
        val specialty1 = getById<Specialty?>(specialties, Specialty::class.java, 1)
        assertThat(specialty1!!.name).isEqualTo("radiology")
        val specialty3 = getById<Specialty?>(specialties, Specialty::class.java, 3)
        assertThat(specialty3!!.name).isEqualTo("dentistry")
    }

    @Test
    @Transactional
    fun shouldInsertSpecialty() {
        var specialties = clinicService!!.findAllSpecialties()
        val found = specialties!!.size
        val specialty = Specialty()
        specialty.name = "dermatologist"
        clinicService!!.saveSpecialty(specialty)
        assertThat(specialty.id!!.toLong()).isNotEqualTo(0)
        specialties = clinicService!!.findAllSpecialties()
        assertThat(specialties!!.size).isEqualTo(found + 1)
    }

    @Test
    @Transactional
    fun shouldUpdateSpecialty() {
        var specialty = clinicService!!.findSpecialtyById(1)
        val oldLastName = specialty!!.name
        val newLastName = oldLastName + "X"
        specialty.name = newLastName
        clinicService!!.saveSpecialty(specialty)
        specialty = clinicService!!.findSpecialtyById(1)
        assertThat(specialty!!.name).isEqualTo(newLastName)
    }

    @Test
    @Transactional
    fun shouldDeleteSpecialty() {
        var specialty: Specialty? = Specialty()
        specialty!!.name = "test"
        clinicService!!.saveSpecialty(specialty)
        val specialtyId = specialty.id
        assertThat(specialtyId).isNotNull()
        specialty = clinicService!!.findSpecialtyById(specialtyId!!)
        assertThat(specialty).isNotNull()
        clinicService!!.deleteSpecialty(specialty!!)
        specialty = try {
            clinicService!!.findSpecialtyById(specialtyId)
        } catch (e: Exception) {
            null
        }
        assertThat(specialty).isNull()
    }
}
