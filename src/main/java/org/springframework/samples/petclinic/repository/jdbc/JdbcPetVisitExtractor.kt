/*
 * Copyright 2002-2015 the original author or authors.
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

import org.springframework.data.jdbc.core.OneToManyResultSetExtractor
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.samples.petclinic.model.Visit
import java.sql.ResultSet
import java.sql.SQLException

/**
 * [ResultSetExtractor] implementation by using the
 * [OneToManyResultSetExtractor] of Spring Data Core JDBC Extensions.
 */
class JdbcPetVisitExtractor : OneToManyResultSetExtractor<JdbcPet?, Visit?, Int?>(JdbcPetRowMapper(), JdbcVisitRowMapper()) {
    @Throws(SQLException::class)
    protected fun mapPrimaryKey(rs: ResultSet): Int {
        return rs.getInt("pets_id")
    }

    @Throws(SQLException::class)
    protected fun mapForeignKey(rs: ResultSet): Int? {
        return if (rs.getObject("visits_pet_id") == null) {
            null
        } else {
            rs.getInt("visits_pet_id")
        }
    }

    protected fun addChild(root: JdbcPet, child: Visit?) {
        root.addVisit(child!!)
    }
}
