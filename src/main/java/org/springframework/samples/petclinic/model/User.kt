package org.springframework.samples.petclinic.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "users")
class User {
    @Id
    @Column(name = "username")
    var username: String? = null

    @Column(name = "password")
    var password: String? = null

    @Column(name = "enabled")
    var enabled: Boolean? = null

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER)
    private var roles: MutableSet<Role>? = null

    fun getRoles(): Set<Role>? {
        return roles
    }

    fun setRoles(roles: MutableSet<Role>?) {
        this.roles = roles
    }

    @JsonIgnore
    fun addRole(roleName: String?) {
        if (roles == null) {
            roles = HashSet()
        }
        val role = Role()
        role.name = roleName
        roles!!.add(role)
    }
}
