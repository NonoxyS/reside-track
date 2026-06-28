package dev.nonoxy.residetrack.feature.rooms.api.store

/** Why a backup operation failed (or was refused) — the UI resolves each to its own message. */
enum class BackupErrorKind {
    /** Export was refused because there is nothing to back up yet. */
    ExportNoData,
    ExportFailed,

    /** The picked file could not be read or parsed as a backup. */
    ImportReadFailed,

    /** The picked file parsed but holds no rooms, so restoring would only wipe existing data. */
    ImportEmpty,
    RestoreFailed,
}
