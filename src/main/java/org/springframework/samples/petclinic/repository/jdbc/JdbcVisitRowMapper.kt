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

import org.springframework.jdbc.core.RowMapper
import org.springframework.samples.petclinic.model.Visit
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

/**
 * [RowMapper] implementation mapping data from a [ResultSet] to the corresponding properties
 * of the [Visit] class.
 */
internal class JdbcVisitRowMapper : RowMapper<Visit?> {
    @Throws(SQLException::class)
    fun mapRow(rs: ResultSet, row: Int): Visit {
        val visit = Visit()
        visit.id = rs.getInt("visit_id")
        val visitDate: Date = rs.getDate("visit_date")
        visit.date = Date(visitDate.time)
        visit.description = rs.getString("description")
        return visit
    }
}
