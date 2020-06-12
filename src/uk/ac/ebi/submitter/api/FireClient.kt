package uk.ac.ebi.uk.ac.ebi.submitter.api

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.util.InternalAPI
import io.ktor.utils.io.streams.asInput
import uk.ac.ebi.uk.ac.ebi.submitter.model.FireFile
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

class FireClient(
    private val httpClient: HttpClient,
    private val messageDigest: MessageDigest
) {
    // TODO move URL to config
    suspend fun listAll(): List<FireFile> = httpClient.get("https://hh.fire-test.sdo.ebi.ac.uk/fire/objects")

    @InternalAPI
    suspend fun saveSubmissionFile(file: File, path: String, accNo: String): FireFile =
        httpClient.post("https://hh.fire-test.sdo.ebi.ac.uk/fire/objects") {
            headers {
                header("x-fire-path", path)
                header("x-fire-size", Files.size(file.toPath()))
                header("x-fire-md5", DatatypeConverter.printHexBinary(messageDigest.digest(file.readBytes())))
            }
            body = MultiPartFormDataContent(formData {
                appendInput("file", Headers.build {
                    append(HttpHeaders.ContentType, "text/plain")
                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                }) { file.inputStream().asInput() }
                append("meta", "{ \"submissionAccNo\": \"$accNo\" }")
            })
        }
}
