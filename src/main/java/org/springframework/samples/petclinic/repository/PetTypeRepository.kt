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
package org.springframework.samples.petclinic.repository

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.PetType

/**
 * @author Vitaliy Fedoriv
 */
interface PetTypeRepository {
    @Throws(DataAccessException::class)
    fun findById(id: Int): PetType?

    @Throws(DataAccessException::class)
    fun findAll(): Collection<PetType?>?

    @Throws(DataAccessException::class)
    fun save(petType: PetType)

    @Throws(DataAccessException::class)
    fun delete(petType: PetType)
}
