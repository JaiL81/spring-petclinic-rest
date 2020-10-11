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
package org.springframework.samples.petclinic.repository

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.BaseEntity
import org.springframework.samples.petclinic.model.Owner

/**
 * Repository class for `Owner` domain objects All method names are compliant with Spring Data naming
 * conventions so this interface can easily be extended for Spring Data See here: http://static.springsource.org/spring-data/jpa/docs/current/reference/html/jpa.repositories.html#jpa.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
interface OwnerRepository {
    /**
     * Retrieve `Owner`s from the data store by last name, returning all owners whose last name *starts*
     * with the given name.
     *
     * @param lastName Value to search for
     * @return a `Collection` of matching `Owner`s (or an empty `Collection` if none
     * found)
     */
    @Throws(DataAccessException::class)
    fun findByLastName(lastName: String): Collection<Owner>?

    /**
     * Retrieve an `Owner` from the data store by id.
     *
     * @param id the id to search for
     * @return the `Owner` if found
     * @throws org.springframework.dao.DataRetrievalFailureException if not found
     */
    @Throws(DataAccessException::class)
    fun findById(id: Int): Owner

    /**
     * Save an `Owner` to the data store, either inserting or updating it.
     *
     * @param owner the `Owner` to save
     * @see BaseEntity.isNew
     */
    @Throws(DataAccessException::class)
    fun save(owner: Owner)

    /**
     * Retrieve `Owner`s from the data store, returning all owners
     *
     * @return a `Collection` of `Owner`s (or an empty `Collection` if none
     * found)
     */
    @Throws(DataAccessException::class)
    fun findAll(): Collection<Owner>?

    /**
     * Delete an `Owner` to the data store by `Owner`.
     *
     * @param owner the `Owner` to delete
     */
    @Throws(DataAccessException::class)
    fun delete(owner: Owner)
}
