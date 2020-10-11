package org.springframework.samples.petclinic.security

import org.springframework.stereotype.Component

@Component
class Roles {
    val OWNER_ADMIN = "ROLE_OWNER_ADMIN"
    val VET_ADMIN = "ROLE_VET_ADMIN"
    val ADMIN = "ROLE_ADMIN"
}
