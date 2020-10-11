package org.springframework.samples.petclinic.repository.jpa

import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.User
import org.springframework.samples.petclinic.repository.UserRepository
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
@Profile("jpa")
class JpaUserRepositoryImpl : UserRepository {
    @PersistenceContext
    private val em: EntityManager? = null

    @Throws(DataAccessException::class)
    override fun save(user: User) {
        if (em.find(User::class.java, user.username) == null) {
            em.persist(user)
        } else {
            em.merge(user)
        }
    }
}
