package org.springframework.samples.petclinic.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.samples.petclinic.model.User
import org.springframework.samples.petclinic.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl : UserService {
    @Autowired
    private val userRepository: UserRepository? = null

    @Transactional
    @Throws(Exception::class)
    override fun saveUser(user: User) {
        if (user.roles == null || user.roles!!.isEmpty()) {
            throw Exception("User must have at least a role set!")
        }
        for (role in user.roles!!) {
            if (!role.name.startsWith("ROLE_")) {
                role.name = "ROLE_" + role.name
            }
            if (role.user == null) {
                role.user = user
            }
        }
        userRepository!!.save(user)
    }
}
