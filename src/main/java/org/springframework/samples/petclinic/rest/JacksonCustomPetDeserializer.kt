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
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Vitaliy Fedoriv
 */
class JacksonCustomPetDeserializer @JvmOverloads constructor(t: Class<Pet?>? = null) : StdDeserializer<Pet?>(t) {
    @Throws(IOException::class, JsonProcessingException::class)
    fun deserialize(parser: JsonParser, context: DeserializationContext?): Pet {
        val formatter = SimpleDateFormat("yyyy/MM/dd")
        val pet = Pet()
        var owner = Owner()
        var petType = PetType()
        val mapper = ObjectMapper()
        var birthDate: Date? = null
        val node: JsonNode = parser.getCodec().readTree(parser)
        val owner_node: JsonNode = node.get("owner")
        val type_node: JsonNode = node.get("type")
        owner = mapper.treeToValue(owner_node, Owner::class.java)
        petType = mapper.treeToValue(type_node, PetType::class.java)
        val petId: Int = node.get("id").asInt()
        val name: String = node.get("name").asText(null)
        val birthDateStr: String = node.get("birthDate").asText(null)
        birthDate = try {
            formatter.parse(birthDateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
            throw IOException(e)
        }
        if (petId != 0) {
            pet.id = petId
        }
        pet.name = name
        pet.birthDate = birthDate
        pet.owner = owner
        pet.type = petType
        return pet
    }
}
