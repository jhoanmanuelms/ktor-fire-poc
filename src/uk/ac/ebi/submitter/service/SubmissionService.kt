package uk.ac.ebi.uk.ac.ebi.submitter.service

import io.ktor.util.InternalAPI
import uk.ac.ebi.uk.ac.ebi.submitter.api.FireClient
import uk.ac.ebi.uk.ac.ebi.submitter.dto.SubmissionRequest
import uk.ac.ebi.uk.ac.ebi.submitter.model.FireFile
import uk.ac.ebi.uk.ac.ebi.submitter.model.Submission
import java.io.File
import java.nio.file.Paths
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

// TODO move this to app properties
const val SUBMISSION_FOLDER = "/submissions/S-BSST/S-BSST0-99"
const val USER_FOLDER = "/tmp/nfs"

class SubmissionService(
    private val fireClient: FireClient,
    private val messageDigest: MessageDigest
) {
    @InternalAPI
    suspend fun submit(request: SubmissionRequest): Submission {
        val relPath = "$SUBMISSION_FOLDER/${request.accNo}"
        val fireFiles =
            loadUserFiles(request.files)
            .map { fireClient.saveSubmissionFile(it, "$relPath/${it.name}", md5(it), request.accNo) }

        return Submission(request.accNo, relPath, fireFiles)
    }

    // TODO this method needs some love
    @InternalAPI
    suspend fun resubmit(request: SubmissionRequest): Submission {
        val accNo = request.accNo
        val relPath = "$SUBMISSION_FOLDER/$accNo"
        val currentFiles =
            findSubmissionFiles(accNo)
                .groupBy { it.filesystemEntry.path }
                .mapValues { it.value.first() }

        loadUserFiles(request.files).forEach {
            val md5 = md5(it)
            val path = "$relPath/${it.name}"
            val currentFile= currentFiles[path]
            when {
                currentFile == null -> fireClient.saveSubmissionFile(it, path, md5, accNo)
                currentFile.objectMd5 != md5 -> {
                    fireClient.deleteFile(currentFile.fireOid)
                    fireClient.saveSubmissionFile(it, path, md5, accNo)
                }
            }
        }

        return Submission(accNo, relPath, findSubmissionFiles(accNo))
    }

    suspend fun findSubmissionFiles(accNo: String): List<FireFile> = fireClient.findSubmissionFiles(accNo)

    suspend fun publish(accNo: String): List<FireFile> =
        fireClient
            .findSubmissionFiles(accNo)
            .map { fireClient.publishFile(it.fireOid) }

    suspend fun unpublish(accNo: String): List<FireFile> =
        fireClient
            .findSubmissionFiles(accNo)
            .map { fireClient.unpublishFile(it.fireOid) }

    private fun loadUserFiles(files: List<String>): List<File> = files.map { Paths.get("$USER_FOLDER/$it").toFile() }

    private fun md5(file: File) = DatatypeConverter.printHexBinary(messageDigest.digest(file.readBytes()))
}
