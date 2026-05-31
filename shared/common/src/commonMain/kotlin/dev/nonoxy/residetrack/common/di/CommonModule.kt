package dev.nonoxy.residetrack.common.di

import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchers
import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchersImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {

    singleOf<CoroutineDispatchers>(::CoroutineDispatchersImpl)
}
