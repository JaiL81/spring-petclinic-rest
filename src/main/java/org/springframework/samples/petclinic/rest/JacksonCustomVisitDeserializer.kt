/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.samples.petclinic.rest

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.Visit
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Vitaliy Fedoriv
 */
class JacksonCustomVisitDeserializer @JvmOverloads constructor(t: Class<Visit?>? = null) : StdDeserializer<Visit?>(t) {
    @Throws(IOException::class, JsonProcessingException::class)
    fun deserialize(parser: JsonParser, context: DeserializationContext?): Visit {
        val formatter = SimpleDateFormat("yyyy/MM/dd")
        val visit = Visit()
        var pet = Pet()
        val mapper = ObjectMapper()
        var visitDate: Date? = null
        val node: JsonNode = parser.getCodec().readTree(parser)
        val pet_node: JsonNode = node.get("pet")
        pet = mapper.treeToValue(pet_node, Pet::class.java)
        val visitId: Int = node.get("id").asInt()
        val visitDateStr: String = node.get("date").asText(null)
        val description: String = node.get("description").asText(null)
        visitDate = try {
            formatter.parse(visitDateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
            throw IOException(e)
        }
        if (visitId != 0) {
            visit.id = visitId
        }
        visit.date = visitDate
        visit.description = description
        visit.pet = pet
        return visit
    }
}
