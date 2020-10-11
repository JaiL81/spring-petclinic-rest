package org.springframework.samples.petclinic.service.clinicService

import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.TestConfiguration

@TestConfiguration
class ApplicationTestConfig {
    init {
        MockitoAnnotations.initMocks(this)
    }
}
