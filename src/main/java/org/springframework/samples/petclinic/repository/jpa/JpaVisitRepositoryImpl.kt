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
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.VisitRepository
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.Query

/**
 * JPA implementation of the ClinicService interface using EntityManager.
 *
 *
 *
 * The mappings are defined in "orm.xml" located in the META-INF directory.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jpa")
class JpaVisitRepositoryImpl : VisitRepository {
    @PersistenceContext
    private val em: EntityManager? = null
    override fun save(visit: Visit) {
        if (visit.id == null) {
            em.persist(visit)
        } else {
            em.merge(visit)
        }
    }

    override fun findByPetId(petId: Int?): List<Visit>? {
        val query: Query = em.createQuery("SELECT v FROM Visit v where v.pet.id= :id")
        query.setParameter("id", petId)
        return query.getResultList()
    }

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Visit? {
        return em.find(Visit::class.java, id)
    }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Visit?>? {
        return em.createQuery("SELECT v FROM Visit v").getResultList()
    }

    @Throws(DataAccessException::class)
    override fun delete(visit: Visit) {
        em.remove(if (em.contains(visit)) visit else em.merge(visit))
    }
}
