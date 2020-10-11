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
package org.springframework.samples.petclinic.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.springframework.beans.support.MutableSortDefinition
import org.springframework.beans.support.PropertyComparator
import org.springframework.core.style.ToStringCreator
import org.springframework.samples.petclinic.rest.JacksonCustomOwnerDeserializer
import org.springframework.samples.petclinic.rest.JacksonCustomOwnerSerializer
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.Digits
import javax.validation.constraints.NotEmpty

/**
 * Simple JavaBean domain object representing an owner.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
@Entity
@Table(name = "owners")
@JsonSerialize(using = JacksonCustomOwnerSerializer::class)
@JsonDeserialize(using = JacksonCustomOwnerDeserializer::class)
class Owner : Person() {
    @Column(name = "address")
    @NotEmpty
    var address: String? = null

    @Column(name = "city")
    @NotEmpty
    var city: String? = null

    @Column(name = "telephone")
    @NotEmpty
    @Digits(fraction = 0, integer = 10)
    var telephone: String? = null

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private var pets: MutableSet<Pet>? = null

    @get:JsonIgnore
    protected var petsInternal: MutableSet<Pet>?
        protected get() {
            if (pets == null) {
                pets = HashSet()
            }
            return pets
        }
        protected set(pets) {
            this.pets = pets
        }

    fun getPets(): List<Pet> {
        val sortedPets: List<Pet> = ArrayList(petsInternal)
        PropertyComparator.sort(sortedPets, MutableSortDefinition("name", true, true))
        return Collections.unmodifiableList(sortedPets)
    }

    fun addPet(pet: Pet) {
        petsInternal!!.add(pet)
        pet.owner = this
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     *
     * @param name to test
     * @return true if pet name is already in use
     */
    fun getPet(name: String): Pet? {
        return getPet(name, false)
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     *
     * @param name to test
     * @return true if pet name is already in use
     */
    fun getPet(name: String, ignoreNew: Boolean): Pet? {
        var name = name
        name = name.toLowerCase()
        for (pet in petsInternal!!) {
            if (!ignoreNew || !pet.isNew) {
                var compName = pet.name
                compName = compName!!.toLowerCase()
                if (compName == name) {
                    return pet
                }
            }
        }
        return null
    }

    override fun toString(): String {
        return ToStringCreator(this)
                .append("id", getId())
                .append("new", this.isNew)
                .append("lastName", getLastName())
                .append("firstName", getFirstName())
                .append("address", address)
                .append("city", city)
                .append("telephone", telephone)
                .toString()
    }
}
