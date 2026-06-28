package dev.nonoxy.residetrack.core.backup.domain.model

/** Distinct from a generic parse failure so the UI can tell "wrong/old backup" from "not a backup". */
class UnsupportedBackupVersionException(val version: Int) :
    Exception("Unsupported backup version: $version")
