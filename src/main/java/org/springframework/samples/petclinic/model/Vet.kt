/*
 * Copyright 2002-2018 the original author or authors.
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
import org.springframework.beans.support.MutableSortDefinition
import org.springframework.beans.support.PropertyComparator
import java.util.*
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.xml.bind.annotation.XmlElement

/**
 * Simple JavaBean domain object representing a veterinarian.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Arjen Poutsma
 */
@Entity
@Table(name = "vets")
class Vet : Person() {
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "vet_specialties", joinColumns = JoinColumn(name = "vet_id"), inverseJoinColumns = JoinColumn(name = "specialty_id"))
    private var specialties: MutableSet<Specialty?>? = null

    @get:JsonIgnore
    protected var specialtiesInternal: MutableSet<Specialty?>?
        protected get() {
            if (specialties == null) {
                specialties = HashSet()
            }
            return specialties
        }
        protected set(specialties) {
            this.specialties = specialties
        }

    @XmlElement
    fun getSpecialties(): List<Specialty> {
        val sortedSpecs: List<Specialty?> = ArrayList(specialtiesInternal)
        PropertyComparator.sort(sortedSpecs, MutableSortDefinition("name", true, true))
        return Collections.unmodifiableList(sortedSpecs)
    }

    @get:JsonIgnore
    val nrOfSpecialties: Int
        get() = specialtiesInternal!!.size

    fun addSpecialty(specialty: Specialty?) {
        specialtiesInternal!!.add(specialty)
    }

    fun clearSpecialties() {
        specialtiesInternal!!.clear()
    }
}
