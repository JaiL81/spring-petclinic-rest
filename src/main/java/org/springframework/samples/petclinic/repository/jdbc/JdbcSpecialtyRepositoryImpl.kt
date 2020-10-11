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
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.repository.SpecialtyRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.sql.DataSource

/**
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jdbc")
class JdbcSpecialtyRepositoryImpl @Autowired constructor(dataSource: DataSource?) : SpecialtyRepository {
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    private val insertSpecialty: SimpleJdbcInsert
    override fun findById(id: Int): Specialty? {
        val specialty: Specialty
        try {
            val params: MutableMap<String, Any> = HashMap()
            params["id"] = id
            specialty = namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, name FROM specialties WHERE id= :id",
                    params,
                    BeanPropertyRowMapper.newInstance(Specialty::class.java))
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Specialty::class.java, id)
        }
        return specialty
    }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Specialty?>? {
        val params: Map<String, Any> = HashMap()
        return namedParameterJdbcTemplate.query(
                "SELECT id, name FROM specialties",
                params,
                BeanPropertyRowMapper.newInstance(Specialty::class.java))
    }

    @Throws(DataAccessException::class)
    override fun save(specialty: Specialty) {
        val parameterSource = BeanPropertySqlParameterSource(specialty)
        if (specialty.isNew) {
            val newKey: Number = insertSpecialty.executeAndReturnKey(parameterSource)
            specialty.id = newKey.intValue()
        } else {
            namedParameterJdbcTemplate.update("UPDATE specialties SET name=:name WHERE id=:id",
                    parameterSource)
        }
    }

    @Throws(DataAccessException::class)
    override fun delete(specialty: Specialty) {
        val params: MutableMap<String, Any?> = HashMap()
        params["id"] = specialty.id
        namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE specialty_id=:id", params)
        namedParameterJdbcTemplate.update("DELETE FROM specialties WHERE id=:id", params)
    }

    init {
        namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
        insertSpecialty = SimpleJdbcInsert(dataSource)
                .withTableName("specialties")
                .usingGeneratedKeyColumns("id")
    }
}
