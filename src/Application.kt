package uk.ac.ebi

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.InternalAPI
import uk.ac.ebi.uk.ac.ebi.submitter.config.ApplicationConfig.fireClient
import uk.ac.ebi.uk.ac.ebi.submitter.config.ApplicationConfig.messageDigest
import uk.ac.ebi.uk.ac.ebi.submitter.service.SubmissionService

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@InternalAPI
fun Application.module() {
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

    val submissionService = SubmissionService(fireClient, messageDigest)

    routing {
        get("/files") {
            call.respond(fireClient.listAll())
        }

        post("/submissions/submit") {
            call.respond(submissionService.submit(call.receive()))
        }

        post("/submissions/resubmit") {
            call.respond(submissionService.resubmit(call.receive()))
        }

        get("/submissions/{accNo}/files") {
            val accNo = call.parameters["accNo"].toString()
            call.respond(submissionService.findSubmissionFiles(accNo))
        }

        post("/submissions/{accNo}/publish") {
            val accNo = call.parameters["accNo"].toString()
            call.respond(submissionService.publish(accNo))
        }

        post("/submissions/{accNo}/unpublish") {
            val accNo = call.parameters["accNo"].toString()
            call.respond(submissionService.unpublish(accNo))
        }
    }
}
