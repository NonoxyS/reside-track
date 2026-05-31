package dev.nonoxy.residetrack.feature.manage_students.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore.State
import dev.nonoxy.residetrack.feature.manage_students.impl.domain.ManageStudentsStoreFactory.Message

internal class ManageStudentsReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetIsLoading -> copy(
            isLoading = msg.isLoading,
            isError = if (msg.isLoading) false else isError,
            errorKind = if (msg.isLoading) null else errorKind,
        )

        is Message.SetError -> copy(
            isLoading = false,
            isError = true,
            errorKind = msg.kind,
        )

        Message.ClearError -> copy(
            isError = false,
            errorKind = null,
        )

        is Message.SetRoomAndStudents -> copy(
            isLoading = false,
            isError = false,
            errorKind = null,
            room = msg.room,
            editableStudents = msg.editableStudents,
        )

        is Message.SetEditableStudents -> copy(
            editableStudents = msg.editableStudents,
        )
    }
}
