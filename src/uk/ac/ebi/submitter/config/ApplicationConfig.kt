package uk.ac.ebi.uk.ac.ebi.submitter.config

import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import uk.ac.ebi.uk.ac.ebi.submitter.api.FireClient
import java.security.MessageDigest

object ApplicationConfig {
    val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")

    val httpClient: HttpClient = HttpClient() {
        install(Auth) {
            basic {
                sendWithoutRequest = true
                username = ""
                password = ""
            }
        }

        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    val fireClient: FireClient = FireClient(httpClient, messageDigest)
}