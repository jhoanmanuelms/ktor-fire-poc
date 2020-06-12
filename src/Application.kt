package uk.ac.ebi

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.auth.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.features.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.UserAgent
import io.ktor.client.features.BrowserUserAgent
import io.ktor.client.features.auth.providers.basic
import io.ktor.util.InternalAPI
import uk.ac.ebi.uk.ac.ebi.submitter.api.FireClient
import uk.ac.ebi.uk.ac.ebi.submitter.config.ApplicationConfig.fireClient
import java.nio.file.Paths
import java.security.MessageDigest

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@InternalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Authentication) {
        basic("myBasicAuth") {
            realm = "Ktor Server"
            validate { if (it.name == "test" && it.password == "password") UserIdPrincipal(it.name) else null }
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

//    val client = HttpClient(Apache) {
//        install(Auth) {
//        }
//        install(JsonFeature) {
//            serializer = GsonSerializer()
//        }
//        install(Logging) {
//            level = LogLevel.HEADERS
//        }
//        BrowserUserAgent() // install default browser-like user-agent
//        // install(UserAgent) { agent = "some user agent" }
//    }
//    runBlocking {
//        // Sample for making a HTTP Client request
//        /*
//        val message = client.post<JsonSampleClass> {
//            url("http://127.0.0.1:8080/path/to/endpoint")
//            contentType(ContentType.Application.Json)
//            body = JsonSampleClass(hello = "world")
//        }
//        */
//    }

    routing {
//        get("/") {
//            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
//        }

        get("/files") {
            call.respond(fireClient.listAll())
        }

        post("/submissions") {
            val file = Paths.get("/home/jhoanmanuelms/EBI/studies/test/test-file.txt").toFile()
            val submissionPath = "/S-BSST/S-BSST0-99/S-BSST2/${file.name}"

            call.respond(fireClient.saveSubmissionFile(file, submissionPath, "S-BSST2")!!)
        }

//        authenticate("myBasicAuth") {
//            get("/protected/route/basic") {
//                val principal = call.principal<UserIdPrincipal>()!!
//                call.respondText("Hello ${principal.name}")
//            }
//        }
//
//        get("/json/jackson") {
//            call.respond(mapOf("hello" to "world"))
//        }
    }
}

//data class JsonSampleClass(val hello: String)

