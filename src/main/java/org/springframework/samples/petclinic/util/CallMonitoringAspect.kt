/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.util

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.jmx.export.annotation.ManagedAttribute
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.util.StopWatch

/**
 * Simple aspect that monitors call count and call invocation time. It uses JMX annotations and therefore can be
 * monitored using any JMX console such as the jConsole
 *
 *
 * This is only useful if you use JPA or JDBC.  Spring-data-jpa doesn't have any correctly annotated classes to join on
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Michael Isvy
 * @since 2.5
 */
@ManagedResource("petclinic:type=CallMonitor")
@Aspect
class CallMonitoringAspect {
    @get:ManagedAttribute
    @set:ManagedAttribute
    var isEnabled = true

    @get:ManagedAttribute
    var callCount = 0
        private set
    private var accumulatedCallTime: Long = 0

    @ManagedOperation
    fun reset() {
        callCount = 0
        accumulatedCallTime = 0
    }

    @get:ManagedAttribute
    val callTime: Long
        get() = if (callCount > 0) accumulatedCallTime / callCount else 0

    @Around("within(@org.springframework.stereotype.Repository *)")
    @Throws(Throwable::class)
    operator fun invoke(joinPoint: ProceedingJoinPoint): Any {
        return if (isEnabled) {
            val sw = StopWatch(joinPoint.toShortString())
            sw.start("invoke")
            try {
                joinPoint.proceed()
            } finally {
                sw.stop()
                synchronized(this) {
                    callCount++
                    accumulatedCallTime += sw.getTotalTimeMillis()
                }
            }
        } else {
            joinPoint.proceed()
        }
    }
}
