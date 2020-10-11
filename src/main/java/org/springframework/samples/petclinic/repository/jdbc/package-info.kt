/**
 * The classes in this package represent the JDBC implementation
 * of PetClinic's persistence layer.
 */
package org.springframework.samples.petclinic.repository.jdbc

import kotlin.jvm.Throws
import org.springframework.samples.petclinic.model.BaseEntity
import org.springframework.samples.petclinic.rest.JacksonCustomPetSerializer
import org.springframework.samples.petclinic.rest.JacksonCustomPetDeserializer
import org.springframework.samples.petclinic.model.NamedEntity
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.model.Person
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.model.Vet
import org.springframework.samples.petclinic.rest.JacksonCustomOwnerSerializer
import org.springframework.samples.petclinic.rest.JacksonCustomOwnerDeserializer
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.rest.JacksonCustomVisitSerializer
import org.springframework.samples.petclinic.rest.JacksonCustomVisitDeserializer
import org.springframework.samples.petclinic.service.UserService
import org.springframework.samples.petclinic.repository.UserRepository
import org.springframework.samples.petclinic.repository.PetRepository
import org.springframework.samples.petclinic.repository.VetRepository
import org.springframework.samples.petclinic.repository.OwnerRepository
import org.springframework.samples.petclinic.repository.VisitRepository
import org.springframework.samples.petclinic.repository.SpecialtyRepository
import org.springframework.samples.petclinic.repository.PetTypeRepository
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.samples.petclinic.repository.jdbc.JdbcPet
import java.sql.SQLException
import java.sql.ResultSet
import org.springframework.samples.petclinic.util.EntityUtils
import org.springframework.samples.petclinic.repository.jdbc.JdbcPetRowMapper
import org.springframework.samples.petclinic.repository.jdbc.JdbcVisitRowMapper
import org.springframework.samples.petclinic.repository.jdbc.JdbcPetVisitExtractor
import org.springframework.samples.petclinic.repository.jdbc.JdbcVisitRepositoryImpl.JdbcVisitRowMapperExt
import org.springframework.samples.petclinic.repository.springdatajpa.PetRepositoryOverride
import org.springframework.samples.petclinic.repository.springdatajpa.VisitRepositoryOverride
import org.springframework.samples.petclinic.repository.springdatajpa.PetTypeRepositoryOverride
import org.springframework.samples.petclinic.repository.springdatajpa.SpecialtyRepositoryOverride
import kotlin.jvm.JvmStatic
