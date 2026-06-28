package dev.nonoxy.residetrack.core.backup.domain.model

/**
 * Thrown by parsing when a backup file is well-formed but its format version is not understood by
 * this build. Distinct from a generic parse failure so the UI can tell "wrong/old backup" from
 * "this isn't a backup file at all".
 */
class UnsupportedBackupVersionException(val version: Int) :
    Exception("Unsupported backup version: $version")
