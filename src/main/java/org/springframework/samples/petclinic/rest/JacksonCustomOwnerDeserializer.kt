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
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.springframework.samples.petclinic.model.Owner
import java.io.IOException

/**
 * @author Vitaliy Fedoriv
 */
class JacksonCustomOwnerDeserializer @JvmOverloads constructor(t: Class<Owner?>? = null) : StdDeserializer<Owner?>(t) {
    @Throws(IOException::class, JsonProcessingException::class)
    fun deserialize(parser: JsonParser, context: DeserializationContext?): Owner {
        val node: JsonNode = parser.getCodec().readTree(parser)
        val owner = Owner()
        val firstName: String = node.get("firstName").asText(null)
        val lastName: String = node.get("lastName").asText(null)
        val address: String = node.get("address").asText(null)
        val city: String = node.get("city").asText(null)
        val telephone: String = node.get("telephone").asText(null)
        if (node.hasNonNull("id")) {
            owner.id = node.get("id").asInt()
        }
        owner.firstName = firstName
        owner.lastName = lastName
        owner.address = address
        owner.city = city
        owner.telephone = telephone
        return owner
    }
}
