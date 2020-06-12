package uk.ac.ebi.uk.ac.ebi.submitter.dto

data class SubmissionRequest(
    val accNo: String,
    val files: List<String>
)
