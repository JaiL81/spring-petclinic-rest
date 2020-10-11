package org.springframework.samples.petclinic.repository.jdbc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.samples.petclinic.model.User
import org.springframework.samples.petclinic.repository.UserRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.sql.DataSource

@Repository
@Profile("jdbc")
class JdbcUserRepositoryImpl @Autowired constructor(dataSource: DataSource?) : UserRepository {
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    private val insertUser: SimpleJdbcInsert

    @Throws(DataAccessException::class)
    override fun save(user: User) {
        val parameterSource = BeanPropertySqlParameterSource(user)
        try {
            getByUsername(user.username)
            namedParameterJdbcTemplate.update("UPDATE users SET password=:password, enabled=:enabled WHERE username=:username", parameterSource)
        } catch (e: EmptyResultDataAccessException) {
            insertUser.execute(parameterSource)
        } finally {
            updateUserRoles(user)
        }
    }

    private fun getByUsername(username: String?): User {
        val params: MutableMap<String, Any?> = HashMap()
        params["username"] = username
        return namedParameterJdbcTemplate.queryForObject("SELECT * FROM users WHERE username=:username",
                params, BeanPropertyRowMapper.newInstance(User::class.java))
    }

    private fun updateUserRoles(user: User) {
        val params: MutableMap<String, Any?> = HashMap()
        params["username"] = user.username
        namedParameterJdbcTemplate.update("DELETE FROM roles WHERE username=:username", params)
        for (role in user.roles!!) {
            params["role"] = role.name
            if (role.name != null) {
                namedParameterJdbcTemplate.update("INSERT INTO roles(username, role) VALUES (:username, :role)", params)
            }
        }
    }

    init {
        namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
        insertUser = SimpleJdbcInsert(dataSource).withTableName("users")
    }
}
