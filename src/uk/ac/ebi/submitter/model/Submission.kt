package uk.ac.ebi.uk.ac.ebi.submitter.model

data class Submission(
    val accNo: String,
    val relPath: String,
    val files: List<FireFile>
)
