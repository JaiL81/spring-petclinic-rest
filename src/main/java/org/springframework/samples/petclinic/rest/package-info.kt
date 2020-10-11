/**
 * The classes in this package represent PetClinic's REST API.
 */
package org.springframework.samples.petclinic.rest

import kotlin.jvm.JvmOverloads
import org.springframework.samples.petclinic.rest.BindingErrorsResponse.BindingError
import org.springframework.samples.petclinic.rest.ExceptionControllerAdvice.ErrorInfo
import kotlin.jvm.Throws
import java.io.IOException
import java.text.SimpleDateFormat
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.samples.petclinic.rest.BindingErrorsResponse
import java.lang.Void
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.service.UserService
import org.springframework.samples.petclinic.model.Vet
