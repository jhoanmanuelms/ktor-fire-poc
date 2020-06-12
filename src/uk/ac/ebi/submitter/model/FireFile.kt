package uk.ac.ebi.uk.ac.ebi.submitter.model

data class FireFile(
    val objectId: Number,
    val fireOid: String,
    val objectMd5: String,
    val objectSize: Number,
    val createTime: String,
    val filesystemEntry: FileSystemEntry,
    val metadata: List<MetadataEntry> = listOf()
)

data class FileSystemEntry(
    val path: String,
    val published: Boolean
)

data class MetadataEntry(
    val key: String,
    val value: String
)
