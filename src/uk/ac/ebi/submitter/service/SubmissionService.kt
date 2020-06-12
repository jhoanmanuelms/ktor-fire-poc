package uk.ac.ebi.uk.ac.ebi.submitter.service

import io.ktor.util.InternalAPI
import uk.ac.ebi.uk.ac.ebi.submitter.api.FireClient
import uk.ac.ebi.uk.ac.ebi.submitter.dto.SubmissionRequest
import uk.ac.ebi.uk.ac.ebi.submitter.model.Submission
import java.io.File
import java.nio.file.Paths

// TODO move this to app properties
const val SUBMISSION_FOLDER = "/submissions/S-BSST/S-BSST0-99"
const val USER_FOLDER = "/nfs/path"

class SubmissionService(private val fireClient: FireClient) {
    @InternalAPI
    suspend fun submit(request: SubmissionRequest): Submission {
        val relPath = "$SUBMISSION_FOLDER/${request.accNo}"
        val fireFiles =
            loadUserFiles(request.files)
            .map { fireClient.saveSubmissionFile(it, "$relPath/${it.name}", request.accNo) }

        return Submission(request.accNo, relPath, fireFiles)
    }

    fun resubmit(request: SubmissionRequest): Submission {
        TODO()
    }

    fun publish(accNo: String) {
        TODO()
    }

    private fun loadUserFiles(files: List<String>): List<File> = files.map { Paths.get("$USER_FOLDER/$it").toFile() }
}