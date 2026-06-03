package dev.nonoxy.residetrack.common.resources

import android.content.Context
import dev.icerock.moko.resources.FileResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidFileResourceReader(
    private val context: Context,
) : FileResourceReader {

    override suspend fun readText(resource: FileResource): String =
        withContext(Dispatchers.IO) {
            resource.readText(context)
        }
}
