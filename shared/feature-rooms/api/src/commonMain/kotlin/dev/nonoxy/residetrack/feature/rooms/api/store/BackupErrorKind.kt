package dev.nonoxy.residetrack.feature.rooms.api.store

enum class BackupErrorKind {
    ExportNoData,
    ExportFailed,
    ImportReadFailed,
    ImportVersionUnsupported,
    ImportEmpty,
    RestoreFailed,
}
