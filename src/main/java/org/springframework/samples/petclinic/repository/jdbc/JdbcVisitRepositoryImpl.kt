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
package org.springframework.samples.petclinic.repository.jdbc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.VisitRepository
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource

/**
 * A simple JDBC-based implementation of the [VisitRepository] interface.
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
class JdbcVisitRepositoryImpl @Autowired constructor(dataSource: DataSource?) : VisitRepository {
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    protected var insertVisit: SimpleJdbcInsert

    /**
     * Creates a [MapSqlParameterSource] based on data values from the supplied [Visit] instance.
     */
    protected fun createVisitParameterSource(visit: Visit): MapSqlParameterSource {
        return MapSqlParameterSource()
                .addValue("id", visit.id)
                .addValue("visit_date", visit.date)
                .addValue("description", visit.description)
                .addValue("pet_id", visit.pet.id)
    }

    override fun findByPetId(petId: Int?): List<Visit>? {
        val params: MutableMap<String, Any?> = HashMap()
        params["id"] = petId
        val pet: JdbcPet = namedParameterJdbcTemplate.queryForObject(
                "SELECT id as pets_id, name, birth_date, type_id, owner_id FROM pets WHERE id=:id",
                params,
                JdbcPetRowMapper())
        val visits: List<Visit> = namedParameterJdbcTemplate.query(
                "SELECT id as visit_id, visit_date, description FROM visits WHERE pet_id=:id",
                params, JdbcVisitRowMapper())
        for (visit in visits) {
            visit.pet = pet
        }
        return visits
    }

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Visit? {
        val visit: Visit
        try {
            val params: MutableMap<String, Any> = HashMap()
            params["id"] = id
            visit = namedParameterJdbcTemplate.queryForObject(
                    "SELECT id as visit_id, visits.pet_id as pets_id, visit_date, description FROM visits WHERE id= :id",
                    params,
                    JdbcVisitRowMapperExt())
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Visit::class.java, id)
        }
        return visit
    }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Visit?>? {
        val params: Map<String, Any> = HashMap()
        return namedParameterJdbcTemplate.query(
                "SELECT id as visit_id, pets.id as pets_id, visit_date, description FROM visits LEFT JOIN pets ON visits.pet_id = pets.id",
                params, JdbcVisitRowMapperExt())
    }

    @Throws(DataAccessException::class)
    override fun save(visit: Visit) {
        if (visit.isNew) {
            val newKey: Number = insertVisit.executeAndReturnKey(createVisitParameterSource(visit))
            visit.id = newKey.intValue()
        } else {
            namedParameterJdbcTemplate.update(
                    "UPDATE visits SET visit_date=:visit_date, description=:description, pet_id=:pet_id WHERE id=:id ",
                    createVisitParameterSource(visit))
        }
    }

    @Throws(DataAccessException::class)
    override fun delete(visit: Visit) {
        val params: MutableMap<String, Any?> = HashMap()
        params["id"] = visit.id
        namedParameterJdbcTemplate.update("DELETE FROM visits WHERE id=:id", params)
    }

    protected inner class JdbcVisitRowMapperExt : RowMapper<Visit?> {
        @Throws(SQLException::class)
        fun mapRow(rs: ResultSet, rowNum: Int): Visit {
            val visit = Visit()
            var pet = JdbcPet()
            var petType = PetType()
            var owner = Owner()
            visit.id = rs.getInt("visit_id")
            val visitDate: Date = rs.getDate("visit_date")
            visit.date = Date(visitDate.time)
            visit.description = rs.getString("description")
            val params: MutableMap<String, Any> = HashMap()
            params["id"] = rs.getInt("pets_id")
            pet = namedParameterJdbcTemplate.queryForObject(
                    "SELECT pets.id as pets_id, name, birth_date, type_id, owner_id FROM pets WHERE pets.id=:id",
                    params,
                    JdbcPetRowMapper())
            params["type_id"] = pet.typeId
            petType = namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, name FROM types WHERE id= :type_id",
                    params,
                    BeanPropertyRowMapper.newInstance(PetType::class.java))
            pet.type = petType
            params["owner_id"] = pet.ownerId
            owner = namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE id= :owner_id",
                    params,
                    BeanPropertyRowMapper.newInstance(Owner::class.java))
            pet.owner = owner
            visit.pet = pet
            return visit
        }
    }

    init {
        namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
        insertVisit = SimpleJdbcInsert(dataSource)
                .withTableName("visits")
                .usingGeneratedKeyColumns("id")
    }
}
