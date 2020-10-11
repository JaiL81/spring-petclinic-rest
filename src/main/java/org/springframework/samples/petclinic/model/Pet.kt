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
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.samples.petclinic.rest.JacksonCustomPetDeserializer
import org.springframework.samples.petclinic.rest.JacksonCustomPetSerializer
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

/**
 * Simple business object representing a pet.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Entity
@Table(name = "pets")
@JsonSerialize(using = JacksonCustomPetSerializer::class)
@JsonDeserialize(using = JacksonCustomPetDeserializer::class)
open class Pet : NamedEntity() {
    @Column(name = "birth_date")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    var birthDate: Date? = null

    @ManyToOne
    @JoinColumn(name = "type_id")
    var type: PetType? = null

    @ManyToOne
    @JoinColumn(name = "owner_id")
    var owner: Owner? = null

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pet", fetch = FetchType.EAGER)
    private var visits: MutableSet<Visit>? = null

    @get:JsonIgnore
    protected var visitsInternal: MutableSet<Visit>?
        protected get() {
            if (visits == null) {
                visits = HashSet()
            }
            return visits
        }
        protected set(visits) {
            this.visits = visits
        }

    fun getVisits(): List<Visit> {
        val sortedVisits: List<Visit> = ArrayList(visitsInternal)
        PropertyComparator.sort(sortedVisits, MutableSortDefinition("date", false, false))
        return Collections.unmodifiableList(sortedVisits)
    }

    fun addVisit(visit: Visit) {
        visitsInternal!!.add(visit)
        visit.pet = this
    }
}
