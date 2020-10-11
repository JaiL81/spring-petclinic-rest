package org.springframework.samples.petclinic.service

import org.springframework.samples.petclinic.model.User

interface UserService {
    @Throws(Exception::class)
    fun saveUser(user: User)
}
