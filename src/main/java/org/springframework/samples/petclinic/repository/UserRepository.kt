package org.springframework.samples.petclinic.repository

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.User

interface UserRepository {
    @Throws(DataAccessException::class)
    fun save(user: User)
}
