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
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.OwnerRepository
import org.springframework.samples.petclinic.util.EntityUtils
import org.springframework.stereotype.Repository
import java.util.*
import javax.sql.DataSource
import javax.transaction.Transactional

/**
 * A simple JDBC-based implementation of the [OwnerRepository] interface.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Thomas Risberg
 * @author Mark Fisher
 * @author Antoine Rey
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jdbc")
class JdbcOwnerRepositoryImpl @Autowired constructor(dataSource: DataSource?) : OwnerRepository {
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    private val insertOwner: SimpleJdbcInsert

    /**
     * Loads [Owners][Owner] from the data store by last name, returning all owners whose last name *starts* with
     * the given name; also loads the [Pets][Pet] and [Visits][Visit] for the corresponding owners, if not
     * already loaded.
     */
    @Throws(DataAccessException::class)
    override fun findByLastName(lastName: String): Collection<Owner>? {
        val params: MutableMap<String, Any> = HashMap()
        params["lastName"] = "$lastName%"
        val owners: List<Owner> = namedParameterJdbcTemplate.query(
                "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE last_name like :lastName",
                params,
                BeanPropertyRowMapper.newInstance(Owner::class.java)
        )
        loadOwnersPetsAndVisits(owners)
        return owners
    }

    /**
     * Loads the [Owner] with the supplied `id`; also loads the [Pets][Pet] and [Visits][Visit]
     * for the corresponding owner, if not already loaded.
     */
    @Throws(DataAccessException::class)
    override fun findById(id: Int): Owner {
        val owner: Owner
        try {
            val params: MutableMap<String, Any> = HashMap()
            params["id"] = id
            owner = namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE id= :id",
                    params,
                    BeanPropertyRowMapper.newInstance(Owner::class.java)
            )
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Owner::class.java, id)
        }
        loadPetsAndVisits(owner)
        return owner
    }

    fun loadPetsAndVisits(owner: Owner) {
        val params: MutableMap<String, Any?> = HashMap()
        params["id"] = owner.id
        val pets: List<JdbcPet> = namedParameterJdbcTemplate.query(
                "SELECT pets.id as pets_id, name, birth_date, type_id, owner_id, visits.id as visit_id, visit_date, description, visits.pet_id as visits_pet_id FROM pets LEFT OUTER JOIN visits ON pets.id = visits.pet_id WHERE owner_id=:id ORDER BY pets.id",
                params,
                JdbcPetVisitExtractor()
        )
        val petTypes = petTypes
        for (pet in pets) {
            pet.type = EntityUtils.getById(petTypes, PetType::class.java, pet.typeId)
            owner.addPet(pet)
        }
    }

    @Throws(DataAccessException::class)
    override fun save(owner: Owner) {
        val parameterSource = BeanPropertySqlParameterSource(owner)
        if (owner.isNew) {
            val newKey: Number = insertOwner.executeAndReturnKey(parameterSource)
            owner.id = newKey.intValue()
        } else {
            namedParameterJdbcTemplate.update(
                    "UPDATE owners SET first_name=:firstName, last_name=:lastName, address=:address, " +
                            "city=:city, telephone=:telephone WHERE id=:id",
                    parameterSource)
        }
    }

    @get:Throws(DataAccessException::class)
    val petTypes: Collection<PetType>
        get() = namedParameterJdbcTemplate.query(
                "SELECT id, name FROM types ORDER BY name", HashMap<String, Any>(),
                BeanPropertyRowMapper.newInstance(PetType::class.java))

    /**
     * Loads the [Pet] and [Visit] data for the supplied [List] of [Owners][Owner].
     *
     * @param owners the list of owners for whom the pet and visit data should be loaded
     * @see .loadPetsAndVisits
     */
    private fun loadOwnersPetsAndVisits(owners: List<Owner>) {
        for (owner in owners) {
            loadPetsAndVisits(owner)
        }
    }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Owner>? {
        val owners: List<Owner> = namedParameterJdbcTemplate.query(
                "SELECT id, first_name, last_name, address, city, telephone FROM owners",
                HashMap<String, Any>(),
                BeanPropertyRowMapper.newInstance(Owner::class.java))
        for (owner in owners) {
            loadPetsAndVisits(owner)
        }
        return owners
    }

    @Transactional
    @Throws(DataAccessException::class)
    override fun delete(owner: Owner) {
        val owner_params: MutableMap<String, Any?> = HashMap()
        owner_params["id"] = owner.id
        val pets = owner.pets
        // cascade delete pets
        for (pet in pets!!) {
            val pet_params: MutableMap<String, Any?> = HashMap()
            pet_params["id"] = pet.id
            // cascade delete visits
            val visits = pet!!.visits
            for (visit in visits!!) {
                val visit_params: MutableMap<String, Any?> = HashMap()
                visit_params["id"] = visit.id
                namedParameterJdbcTemplate.update("DELETE FROM visits WHERE id=:id", visit_params)
            }
            namedParameterJdbcTemplate.update("DELETE FROM pets WHERE id=:id", pet_params)
        }
        namedParameterJdbcTemplate.update("DELETE FROM owners WHERE id=:id", owner_params)
    }

    init {
        insertOwner = SimpleJdbcInsert(dataSource)
                .withTableName("owners")
                .usingGeneratedKeyColumns("id")
        namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
    }
}
