package dev.nonoxy.residetrack.common.resources

import dev.icerock.moko.resources.FileResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class IosFileResourceReader : FileResourceReader {

    override suspend fun readText(resource: FileResource): String =
        withContext(Dispatchers.Default) {
            resource.readText()
        }
}
