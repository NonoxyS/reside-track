package dev.nonoxy.residetrack.feature.add_room.presentation.models

import dev.icerock.moko.resources.StringResource
import dev.nonoxy.residetrack.res.MR

/**
 * Inline validation error for a single input field. Only the argument-free
 * "required" kinds ever surface on a field; other AddRoomErrorKind values are
 * shown as a snackbar via the label mapper.
 */
enum class UiAddRoomFieldError {
    FLOOR_REQUIRED,
    ROOM_NUMBER_REQUIRED,
    BEDS_REQUIRED;

    // Computed, not a constructor arg: keeps enum init free of moko-resources so
    // pure mapping logic (and its native unit tests) never trigger MR class init.
    val message: StringResource
        get() = when (this) {
            FLOOR_REQUIRED -> MR.strings.add_room_validation_floor_number_required
            ROOM_NUMBER_REQUIRED -> MR.strings.add_room_validation_room_number_required
            BEDS_REQUIRED -> MR.strings.add_room_validation_beds_count_required
        }
}
