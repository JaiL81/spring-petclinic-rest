/*
 * Copyright 2002-2018 the original author or authors.
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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.model.Vet
import org.springframework.samples.petclinic.repository.VetRepository
import org.springframework.samples.petclinic.util.EntityUtils
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource

/**
 * A simple JDBC-based implementation of the [VetRepository] interface.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Thomas Risberg
 * @author Mark Fisher
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jdbc")
class JdbcVetRepositoryImpl @Autowired constructor(dataSource: DataSource?, jdbcTemplate: JdbcTemplate) : VetRepository {
    private val jdbcTemplate: JdbcTemplate
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    private val insertVet: SimpleJdbcInsert

    /**
     * Refresh the cache of Vets that the ClinicService is holding.
     */
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Vet>? {
        val vets: MutableList<Vet> = ArrayList()
        // Retrieve the list of all vets.
        vets.addAll(jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM vets ORDER BY last_name,first_name",
                BeanPropertyRowMapper.newInstance(Vet::class.java)))

        // Retrieve the list of all possible specialties.
        val specialties: List<Specialty> = jdbcTemplate.query(
                "SELECT id, name FROM specialties",
                BeanPropertyRowMapper.newInstance(Specialty::class.java))

        // Build each vet's list of specialties.
        for (vet in vets) {
            val vetSpecialtiesIds: List<Int> = jdbcTemplate.query(
                    "SELECT specialty_id FROM vet_specialties WHERE vet_id=?",
                    object : BeanPropertyRowMapper<Int?>() {
                        @Throws(SQLException::class)
                        fun mapRow(rs: ResultSet, row: Int): Int {
                            return rs.getInt(1)
                        }
                    },
                    vet.id)
            for (specialtyId in vetSpecialtiesIds) {
                val specialty = EntityUtils.getById(specialties, Specialty::class.java, specialtyId)
                vet.addSpecialty(specialty)
            }
        }
        return vets
    }

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Vet? {
        val vet: Vet
        try {
            val vet_params: MutableMap<String, Any> = HashMap()
            vet_params["id"] = id
            vet = namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, first_name, last_name FROM vets WHERE id= :id",
                    vet_params,
                    BeanPropertyRowMapper.newInstance(Vet::class.java))
            val specialties: List<Specialty> = namedParameterJdbcTemplate.query(
                    "SELECT id, name FROM specialties", vet_params, BeanPropertyRowMapper.newInstance(Specialty::class.java))
            val vetSpecialtiesIds: List<Int> = namedParameterJdbcTemplate.query(
                    "SELECT specialty_id FROM vet_specialties WHERE vet_id=:id",
                    vet_params,
                    object : BeanPropertyRowMapper<Int?>() {
                        @Throws(SQLException::class)
                        fun mapRow(rs: ResultSet, row: Int): Int {
                            return rs.getInt(1)
                        }
                    })
            for (specialtyId in vetSpecialtiesIds) {
                val specialty = EntityUtils.getById(specialties, Specialty::class.java, specialtyId)
                vet.addSpecialty(specialty)
            }
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Vet::class.java, id)
        }
        return vet
    }

    @Throws(DataAccessException::class)
    override fun save(vet: Vet) {
        val parameterSource = BeanPropertySqlParameterSource(vet)
        if (vet.isNew) {
            val newKey: Number = insertVet.executeAndReturnKey(parameterSource)
            vet.id = newKey.intValue()
            updateVetSpecialties(vet)
        } else {
            namedParameterJdbcTemplate
                    .update("UPDATE vets SET first_name=:firstName, last_name=:lastName WHERE id=:id", parameterSource)
            updateVetSpecialties(vet)
        }
    }

    @Throws(DataAccessException::class)
    override fun delete(vet: Vet) {
        val params: MutableMap<String, Any?> = HashMap()
        params["id"] = vet.id
        namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE vet_id=:id", params)
        namedParameterJdbcTemplate.update("DELETE FROM vets WHERE id=:id", params)
    }

    @Throws(DataAccessException::class)
    private fun updateVetSpecialties(vet: Vet) {
        val params: MutableMap<String, Any?> = HashMap()
        params["id"] = vet.id
        namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE vet_id=:id", params)
        for (spec in vet.specialties) {
            params["spec_id"] = spec.id
            if (spec.id != null) {
                namedParameterJdbcTemplate.update("INSERT INTO vet_specialties VALUES (:id, :spec_id)", params)
            }
        }
    }

    init {
        this.jdbcTemplate = jdbcTemplate
        insertVet = SimpleJdbcInsert(dataSource).withTableName("vets").usingGeneratedKeyColumns("id")
        namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
    }
}
