package uk.ac.ebi.uk.ac.ebi.submitter.api

import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.util.InternalAPI
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.streams.asInput
import uk.ac.ebi.uk.ac.ebi.submitter.model.FireFile
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

class FireClient(private val messageDigest: MessageDigest, private val httpClient: HttpClient) {


    suspend fun listAll(): List<FireFile> = httpClient.get("https://hh.fire-test.sdo.ebi.ac.uk/fire/objects")

    @InternalAPI
    suspend fun saveSubmissionFile(file: File, path: String, accNo: String): FireFile? {
        val fileSize = Files.size(file.toPath())
        val md5 = DatatypeConverter.printHexBinary(messageDigest.digest(file.readBytes()))
        val metadata = "{ \"submissionAccNo\": \"$accNo\" }"
        val response = httpClient.post<FireFile>("https://hh.fire-test.sdo.ebi.ac.uk/fire/objects") {
                headers {
                    header("x-fire-path", path)
                    header("x-fire-size", fileSize)
                    header("x-fire-md5", md5)
                }
                body = MultiPartFormDataContent(formData {
//                    append(FormPart("file", file))

                    appendInput("file", Headers.build {
                        append(HttpHeaders.ContentType, "text/plain")
                        append(HttpHeaders.ContentDisposition, "filename=test-file.txt")
                    }) { file.inputStream().asInput() }
                    append("meta", metadata)
//                append("file", file.inputStream().asInput(), Headers.build {
//                    append(HttpHeaders.ContentType, "")
//                })
//                    appendInput(key = "file", size = fileSize) { file.inputStream().asInput() }
                })
            }

        return response
    }
}
