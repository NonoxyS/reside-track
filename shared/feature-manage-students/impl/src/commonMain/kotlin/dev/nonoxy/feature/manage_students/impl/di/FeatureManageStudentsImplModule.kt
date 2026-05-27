package dev.nonoxy.feature.manage_students.impl.di

import dev.nonoxy.common.coroutines.CoroutineDispatchers
import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.feature.manage_students.impl.domain.ManageStudentsStoreFactory
import org.koin.dsl.bind
import org.koin.dsl.module

val featureManageStudentsImplModule = module {

    factory { (mode: ManageStudentsMode) ->
        ManageStudentsStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create(mode = mode)
    } bind ManageStudentsStore::class
}
