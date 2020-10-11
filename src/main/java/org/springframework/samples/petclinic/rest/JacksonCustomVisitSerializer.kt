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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.samples.petclinic.model.Visit
import java.io.IOException
import java.text.Format
import java.text.SimpleDateFormat

/**
 * @author Vitaliy Fedoriv
 */
class JacksonCustomVisitSerializer protected constructor(t: Class<Visit?>?) : StdSerializer<Visit?>(t) {
    constructor() : this(null) {}

    @Throws(IOException::class)
    fun serialize(visit: Visit?, jgen: JsonGenerator, provider: SerializerProvider?) {
        if (visit == null || visit.pet == null) {
            throw IOException("Cannot serialize Visit object - visit or visit.pet is null")
        }
        val formatter: Format = SimpleDateFormat("yyyy/MM/dd")
        jgen.writeStartObject() // visit
        if (visit.id == null) {
            jgen.writeNullField("id")
        } else {
            jgen.writeNumberField("id", visit.id)
        }
        jgen.writeStringField("date", formatter.format(visit.date))
        jgen.writeStringField("description", visit.description)
        val pet = visit.pet
        jgen.writeObjectFieldStart("pet")
        if (pet.id == null) {
            jgen.writeNullField("id")
        } else {
            jgen.writeNumberField("id", pet.id)
        }
        jgen.writeStringField("name", pet.name)
        jgen.writeStringField("birthDate", formatter.format(pet.birthDate))
        val petType = pet.type
        jgen.writeObjectFieldStart("type")
        if (petType.id == null) {
            jgen.writeNullField("id")
        } else {
            jgen.writeNumberField("id", petType.id)
        }
        jgen.writeStringField("name", petType.name)
        jgen.writeEndObject() // type
        val owner = pet.owner
        jgen.writeObjectFieldStart("owner")
        if (owner.id == null) {
            jgen.writeNullField("id")
        } else {
            jgen.writeNumberField("id", owner.id)
        }
        jgen.writeStringField("firstName", owner.firstName)
        jgen.writeStringField("lastName", owner.lastName)
        jgen.writeStringField("address", owner.address)
        jgen.writeStringField("city", owner.city)
        jgen.writeStringField("telephone", owner.telephone)
        jgen.writeEndObject() // owner
        jgen.writeEndObject() // pet
        jgen.writeEndObject() // visit
    }
}
