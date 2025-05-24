package by.vpolkhovsy

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        environment {
            config = ConfigLoader.load("application.yaml")
            log = LoggerFactory.getLogger("ktor.test")
        }

    }
}
