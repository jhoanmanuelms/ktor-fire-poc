package uk.ac.ebi.uk.ac.ebi.submitter.api

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.util.InternalAPI
import io.ktor.utils.io.streams.asInput
import uk.ac.ebi.uk.ac.ebi.submitter.dto.SubmissionMetadata
import uk.ac.ebi.uk.ac.ebi.submitter.model.FireFile
import java.io.File
import java.nio.file.Files

// TODO move URL to config
const val FIRE_HOST = "https://hh.fire-test.sdo.ebi.ac.uk/fire"

class FireClient(private val httpClient: HttpClient) {
    suspend fun listAll(): List<FireFile> = httpClient.get("$FIRE_HOST/objects")

    @InternalAPI
    suspend fun saveSubmissionFile(file: File, path: String, md5: String, accNo: String): FireFile =
        httpClient.post("$FIRE_HOST/objects") {
            headers {
                header("x-fire-path", path)
                header("x-fire-size", Files.size(file.toPath()))
                header("x-fire-md5", md5)
            }
            body = MultiPartFormDataContent(formData {
                appendInput("file", Headers.build {
                    append(HttpHeaders.ContentType, "text/plain")
                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                }) { file.inputStream().asInput() }
                append("meta", "{ \"submissionAccNo\": \"$accNo\" }")
            })
        }

    suspend fun findSubmissionFiles(accNo: String): List<FireFile> =
        httpClient.post("$FIRE_HOST/objects/metadata") {
            header(HttpHeaders.ContentType, "application/json")
            body = SubmissionMetadata(submissionAccNo = accNo)
        }

    suspend fun deleteFile(id: String): Unit = httpClient.delete("$FIRE_HOST/objects/$id")

    suspend fun publishFile(id: String): FireFile = httpClient.put("$FIRE_HOST/objects/$id/publish")

    suspend fun unpublishFile(id: String): FireFile = httpClient.delete("$FIRE_HOST/objects/$id/publish")
}
