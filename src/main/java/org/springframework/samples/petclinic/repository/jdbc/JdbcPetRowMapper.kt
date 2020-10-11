/*
 * Copyright 2002-2013 the original author or authors.
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

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

/**
 * [RowMapper] implementation mapping data from a [ResultSet] to the corresponding properties
 * of the [JdbcPet] class.
 */
class JdbcPetRowMapper : RowMapper<JdbcPet?> {
    @Throws(SQLException::class)
    fun mapRow(rs: ResultSet, rownum: Int): JdbcPet {
        val pet = JdbcPet()
        pet.id = rs.getInt("pets_id")
        pet.name = rs.getString("name")
        val birthDate: Date = rs.getDate("birth_date")
        pet.birthDate = Date(birthDate.time)
        pet.typeId = rs.getInt("type_id")
        pet.ownerId = rs.getInt("owner_id")
        return pet
    }
}
