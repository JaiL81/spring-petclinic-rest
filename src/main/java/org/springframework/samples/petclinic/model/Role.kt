package org.springframework.samples.petclinic.model

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(name = "roles", uniqueConstraints = UniqueConstraint(columnNames = ["username", "role"]))
class Role : BaseEntity() {
    @ManyToOne
    @JoinColumn(name = "username")
    @JsonIgnore
    var user: User? = null

    @Column(name = "role")
    var name: String? = null

}
