package dev.nonoxy.residetrack.feature.upcoming.impl.di

import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchers
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore
import dev.nonoxy.residetrack.feature.upcoming.impl.domain.UpcomingStoreFactory
import org.koin.dsl.module

val featureUpcomingImplModule = module {

    factory<UpcomingStore> {
        UpcomingStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create()
    }
}
