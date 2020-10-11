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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.repository.OwnerRepository
import org.springframework.samples.petclinic.repository.PetRepository
import org.springframework.samples.petclinic.repository.VisitRepository
import org.springframework.samples.petclinic.util.EntityUtils
import org.springframework.stereotype.Repository
import java.util.*
import javax.sql.DataSource

/**
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Thomas Risberg
 * @author Mark Fisher
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jdbc")
class JdbcPetRepositoryImpl @Autowired constructor(dataSource: DataSource?,
                                                   ownerRepository: OwnerRepository,
                                                   visitRepository: VisitRepository) : PetRepository {
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    private val insertPet: SimpleJdbcInsert
    private val ownerRepository: OwnerRepository
    private val visitRepository: VisitRepository

    @Throws(DataAccessException::class)
    override fun findPetTypes(): List<PetType?>? {
        val params: Map<String, Any> = HashMap()
        return namedParameterJdbcTemplate.query(
                "SELECT id, name FROM types ORDER BY name",
                params,
                BeanPropertyRowMapper.newInstance(PetType::class.java))
    }

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Pet? {
        val ownerId: Int
        try {
            val params: MutableMap<String, Any> = HashMap()
            params["id"] = id
            ownerId = namedParameterJdbcTemplate.queryForObject("SELECT owner_id FROM pets WHERE id=:id", params, Int::class.java)
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Pet::class.java, id)
        }
        val owner = ownerRepository.findById(ownerId)
        return EntityUtils.getById(owner!!.pets, Pet::class.java, id)
    }

    @Throws(DataAccessException::class)
    override fun save(pet: Pet) {
        if (pet.isNew) {
            val newKey: Number = insertPet.executeAndReturnKey(
                    createPetParameterSource(pet))
            pet.id = newKey.intValue()
        } else {
            namedParameterJdbcTemplate.update(
                    "UPDATE pets SET name=:name, birth_date=:birth_date, type_id=:type_id, " +
                            "owner_id=:owner_id WHERE id=:id",
                    createPetParameterSource(pet))
        }
    }

    /**
     * Creates a [MapSqlParameterSource] based on data values from the supplied [Pet] instance.
     */
    private fun createPetParameterSource(pet: Pet): MapSqlParameterSource {
        return MapSqlParameterSource()
                .addValue("id", pet.id)
                .addValue("name", pet.name)
                .addValue("birth_date", pet.birthDate)
                .addValue("type_id", pet.type.id)
                .addValue("owner_id", pet.owner.id)
    }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Pet?>? {
        val params: Map<String, Any> = HashMap()
        val pets: MutableCollection<Pet?> = ArrayList()
        var jdbcPets: Collection<JdbcPet> = ArrayList()
        jdbcPets = namedParameterJdbcTemplate
                .query("SELECT pets.id as pets_id, name, birth_date, type_id, owner_id FROM pets",
                        params,
                        JdbcPetRowMapper())
        val petTypes: Collection<PetType> = namedParameterJdbcTemplate.query("SELECT id, name FROM types ORDER BY name",
                HashMap<String, Any>(), BeanPropertyRowMapper.newInstance(PetType::class.java))
        val owners: Collection<Owner> = namedParameterJdbcTemplate.query(
                "SELECT id, first_name, last_name, address, city, telephone FROM owners ORDER BY last_name",
                HashMap<String, Any>(),
                BeanPropertyRowMapper.newInstance(Owner::class.java))
        for (jdbcPet in jdbcPets) {
            jdbcPet.type = EntityUtils.getById(petTypes, PetType::class.java, jdbcPet.typeId)
            jdbcPet.owner = EntityUtils.getById(owners, Owner::class.java, jdbcPet.ownerId)
            // TODO add visits
            pets.add(jdbcPet)
        }
        return pets
    }

    @Throws(DataAccessException::class)
    override fun delete(pet: Pet) {
        val pet_params: MutableMap<String, Any?> = HashMap()
        pet_params["id"] = pet.id
        val visits = pet.visits
        // cascade delete visits
        for (visit in visits!!) {
            val visit_params: MutableMap<String, Any?> = HashMap()
            visit_params["id"] = visit.id
            namedParameterJdbcTemplate.update("DELETE FROM visits WHERE id=:id", visit_params)
        }
        namedParameterJdbcTemplate.update("DELETE FROM pets WHERE id=:id", pet_params)
    }

    init {
        namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
        insertPet = SimpleJdbcInsert(dataSource)
                .withTableName("pets")
                .usingGeneratedKeyColumns("id")
        this.ownerRepository = ownerRepository
        this.visitRepository = visitRepository
    }
}
