package dev.nonoxy.residetrack.common.resources

import dev.icerock.moko.resources.FileResource

/**
 * Reads the text content of a moko [FileResource] from common code.
 *
 * moko exposes platform-specific read APIs (Android needs a `Context`, Apple reads from the bundle),
 * so this interface hides the difference behind a single suspend call. Implementations dispatch the
 * blocking read off the main thread.
 */
interface FileResourceReader {

    suspend fun readText(resource: FileResource): String
}
