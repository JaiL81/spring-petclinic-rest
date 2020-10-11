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

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import java.util.*

/**
 * @author Vitaliy Fedoriv
 */
class BindingErrorsResponse(pathId: Int?, bodyId: Int?) {
    @JvmOverloads
    constructor(id: Int? = null) : this(null, id) {
    }

    private fun addBodyIdError(bodyId: Int?, message: String) {
        val error = BindingError()
        error.setObjectName("body")
        error.setFieldName("id")
        error.setFieldValue(bodyId.toString())
        error.setErrorMessage(message)
        addError(error)
    }

    private val bindingErrors: MutableList<BindingError> = ArrayList()
    fun addError(bindingError: BindingError) {
        bindingErrors.add(bindingError)
    }

    fun addAllErrors(bindingResult: BindingResult) {
        for (fieldError in bindingResult.getFieldErrors()) {
            val error = BindingError()
            error.setObjectName(fieldError.getObjectName())
            error.setFieldName(fieldError.getField())
            error.setFieldValue(java.lang.String.valueOf(fieldError.getRejectedValue()))
            error.setErrorMessage(fieldError.getDefaultMessage())
            addError(error)
        }
    }

    fun toJSON(): String {
        val mapper = ObjectMapper()
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
        var errorsAsJSON = ""
        try {
            errorsAsJSON = mapper.writeValueAsString(bindingErrors)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
        return errorsAsJSON
    }

    override fun toString(): String {
        return "BindingErrorsResponse [bindingErrors=$bindingErrors]"
    }

    protected class BindingError {
        private var objectName = ""
        private var fieldName = ""
        private var fieldValue = ""
        private var errorMessage = ""
        fun setObjectName(objectName: String) {
            this.objectName = objectName
        }

        fun setFieldName(fieldName: String) {
            this.fieldName = fieldName
        }

        fun setFieldValue(fieldValue: String) {
            this.fieldValue = fieldValue
        }

        fun setErrorMessage(error_message: String) {
            errorMessage = error_message
        }

        override fun toString(): String {
            return ("BindingError [objectName=" + objectName + ", fieldName=" + fieldName + ", fieldValue=" + fieldValue
                    + ", errorMessage=" + errorMessage + "]")
        }

    }

    init {
        val onlyBodyIdSpecified = pathId == null && bodyId != null
        if (onlyBodyIdSpecified) {
            addBodyIdError(bodyId, "must not be specified")
        }
        val bothIdsSpecified = pathId != null && bodyId != null
        if (bothIdsSpecified && pathId != bodyId) {
            addBodyIdError(bodyId, String.format("does not match pathId: %d", pathId))
        }
    }
}
