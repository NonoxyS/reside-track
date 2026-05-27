package dev.nonoxy.feature.manage_students.presentation.mappers

import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.feature.manage_students.presentation.models.UiManageStudentsLabel

internal interface UiManageStudentsLabelMapper {
    fun map(item: ManageStudentsStore.Label): UiManageStudentsLabel
}

internal class UiManageStudentsLabelMapperImpl : UiManageStudentsLabelMapper {

    override fun map(item: ManageStudentsStore.Label): UiManageStudentsLabel = when (item) {
        ManageStudentsStore.Label.NavigateBack -> UiManageStudentsLabel.NavigateBack
        is ManageStudentsStore.Label.ShowError -> UiManageStudentsLabel.ShowError(kind = item.kind)
        is ManageStudentsStore.Label.ShowSuccess -> UiManageStudentsLabel.ShowSuccess(kind = item.kind)
    }
}
