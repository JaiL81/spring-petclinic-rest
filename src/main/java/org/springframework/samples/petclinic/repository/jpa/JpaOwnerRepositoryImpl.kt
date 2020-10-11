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
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.repository.OwnerRepository
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.Query

/**
 * JPA implementation of the [OwnerRepository] interface.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jpa")
class JpaOwnerRepositoryImpl : OwnerRepository {
    @PersistenceContext
    private val em: EntityManager? = null

    /**
     * Important: in the current version of this method, we load Owners with all their Pets and Visits while
     * we do not need Visits at all and we only need one property from the Pet objects (the 'name' property).
     * There are some ways to improve it such as:
     * - creating a Ligtweight class (example here: https://community.jboss.org/wiki/LightweightClass)
     * - Turning on lazy-loading and using [OpenSessionInViewFilter]
     */
    override fun findByLastName(lastName: String): Collection<Owner>? {
        // using 'join fetch' because a single query should load both owners and pets
        // using 'left join fetch' because it might happen that an owner does not have pets yet
        val query: Query = em.createQuery("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName")
        query.setParameter("lastName", "$lastName%")
        return query.getResultList()
    }

    override fun findById(id: Int): Owner {
        // using 'join fetch' because a single query should load both owners and pets
        // using 'left join fetch' because it might happen that an owner does not have pets yet
        val query: Query = em.createQuery("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id")
        query.setParameter("id", id)
        return query.getSingleResult()
    }

    override fun save(owner: Owner) {
        if (owner.id == null) {
            em.persist(owner)
        } else {
            em.merge(owner)
        }
    }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Owner>? {
        val query: Query = em.createQuery("SELECT owner FROM Owner owner")
        return query.getResultList()
    }

    @Throws(DataAccessException::class)
    override fun delete(owner: Owner) {
        em.remove(if (em.contains(owner)) owner else em.merge(owner))
    }
}
