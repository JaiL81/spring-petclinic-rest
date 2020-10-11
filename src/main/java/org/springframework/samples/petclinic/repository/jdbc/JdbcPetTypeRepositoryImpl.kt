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
package org.springframework.samples.petclinic.repository.jdbc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.PetTypeRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.sql.DataSource

/**
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jdbc")
class JdbcPetTypeRepositoryImpl @Autowired constructor(dataSource: DataSource?) : PetTypeRepository {
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    private val insertPetType: SimpleJdbcInsert
    override fun findById(id: Int): PetType? {
        val petType: PetType
        try {
            val params: MutableMap<String, Any> = HashMap()
            params["id"] = id
            petType = namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, name FROM types WHERE id= :id",
                    params,
                    BeanPropertyRowMapper.newInstance(PetType::class.java))
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(PetType::class.java, id)
        }
        return petType
    }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<PetType?>? {
        val params: Map<String, Any> = HashMap()
        return namedParameterJdbcTemplate.query(
                "SELECT id, name FROM types",
                params,
                BeanPropertyRowMapper.newInstance(PetType::class.java))
    }

    @Throws(DataAccessException::class)
    override fun save(petType: PetType) {
        val parameterSource = BeanPropertySqlParameterSource(petType)
        if (petType.isNew) {
            val newKey: Number = insertPetType.executeAndReturnKey(parameterSource)
            petType.id = newKey.intValue()
        } else {
            namedParameterJdbcTemplate.update("UPDATE types SET name=:name WHERE id=:id",
                    parameterSource)
        }
    }

    @Throws(DataAccessException::class)
    override fun delete(petType: PetType) {
        val pettype_params: MutableMap<String, Any?> = HashMap()
        pettype_params["id"] = petType.id
        var pets: List<Pet> = ArrayList()
        pets = namedParameterJdbcTemplate.query("SELECT pets.id, name, birth_date, type_id, owner_id FROM pets WHERE type_id=:id",
                pettype_params,
                BeanPropertyRowMapper.newInstance(Pet::class.java))
        // cascade delete pets
        for (pet in pets) {
            val pet_params: MutableMap<String, Any?> = HashMap()
            pet_params["id"] = pet.id
            var visits: List<Visit> = ArrayList()
            visits = namedParameterJdbcTemplate.query(
                    "SELECT id, pet_id, visit_date, description FROM visits WHERE pet_id = :id",
                    pet_params,
                    BeanPropertyRowMapper.newInstance(Visit::class.java))
            // cascade delete visits
            for (visit in visits) {
                val visit_params: MutableMap<String, Any?> = HashMap()
                visit_params["id"] = visit.id
                namedParameterJdbcTemplate.update("DELETE FROM visits WHERE id=:id", visit_params)
            }
            namedParameterJdbcTemplate.update("DELETE FROM pets WHERE id=:id", pet_params)
        }
        namedParameterJdbcTemplate.update("DELETE FROM types WHERE id=:id", pettype_params)
    }

    init {
        namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
        insertPetType = SimpleJdbcInsert(dataSource)
                .withTableName("types")
                .usingGeneratedKeyColumns("id")
    }
}
