package dev.nonoxy.residetrack.common.resources.di

import dev.nonoxy.residetrack.common.resources.AndroidFileResourceReader
import dev.nonoxy.residetrack.common.resources.AndroidStringConverter
import dev.nonoxy.residetrack.common.resources.FileResourceReader
import dev.nonoxy.residetrack.common.resources.StringConverter
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module

internal actual val platformResourcesModule: Module = module {

    factory<StringConverter> { new(::AndroidStringConverter) }

    factory<FileResourceReader> { new(::AndroidFileResourceReader) }
}
