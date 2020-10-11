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
package org.springframework.samples.petclinic.repository.jpa

import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.repository.PetRepository
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * JPA implementation of the [PetRepository] interface.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jpa")
class JpaPetRepositoryImpl : PetRepository {
    @PersistenceContext
    private val em: EntityManager? = null
    override fun findPetTypes(): List<PetType?>? {
        return em.createQuery("SELECT ptype FROM PetType ptype ORDER BY ptype.name").getResultList()
    }

    override fun findById(id: Int): Pet? {
        return em.find(Pet::class.java, id)
    }

    override fun save(pet: Pet) {
        if (pet.id == null) {
            em.persist(pet)
        } else {
            em.merge(pet)
        }
    }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Pet?>? {
        return em.createQuery("SELECT pet FROM Pet pet").getResultList()
    }

    @Throws(DataAccessException::class)
    override fun delete(pet: Pet) {
        //this.em.remove(this.em.contains(pet) ? pet : this.em.merge(pet));
        val petId = pet.id.toString()
        em.createQuery("DELETE FROM Visit visit WHERE pet_id=$petId").executeUpdate()
        em.createQuery("DELETE FROM Pet pet WHERE id=$petId").executeUpdate()
        if (em.contains(pet)) {
            em.remove(pet)
        }
    }
}
