package dev.nonoxy.residetrack.core.notifications.android

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.nonoxy.residetrack.core.notifications.domain.CheckoutDigest
import dev.nonoxy.residetrack.core.notifications.domain.LocalNotifier
import dev.nonoxy.residetrack.res.MR
import java.util.concurrent.TimeUnit

/**
 * Планирует пуши о выезде через WorkManager: день-точность обходит ограничение
 * `SCHEDULE_EXACT_ALARM` и переживает перезапуск процесса.
 *
 * Реальный системный запрос разрешения на Android 13+ требует Activity, поэтому здесь
 * [requestPermission] лишь сообщает текущее состояние; показ диалога инициируется из UI.
 */
internal class AndroidLocalNotifier(
    private val context: Context,
) : LocalNotifier {

    override suspend fun requestPermission(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    override suspend fun sync(digests: List<CheckoutDigest>) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(WORK_TAG)

        val now = System.currentTimeMillis()
        digests.forEach { digest ->
            val delayMillis = digest.fireAtEpochMillis - now
            if (delayMillis < 0) return@forEach

            val request = OneTimeWorkRequestBuilder<CheckoutNotificationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .setInputData(
                    workDataOf(
                        CheckoutNotificationWorker.KEY_NOTIFICATION_ID to digest.date.hashCode(),
                        CheckoutNotificationWorker.KEY_TITLE to title(),
                        CheckoutNotificationWorker.KEY_BODY to body(digest),
                    ),
                )
                .build()

            workManager.enqueueUniqueWork("$WORK_NAME_PREFIX${digest.date}", ExistingWorkPolicy.REPLACE, request)
        }
    }

    private fun title(): String =
        context.getString(MR.strings.notification_checkout_title.resourceId)

    private fun body(digest: CheckoutDigest): String {
        val rooms = context.getString(
            MR.strings.notification_checkout_rooms.resourceId,
            digest.roomNumbers.joinToString(separator = ", "),
        )
        val places = context.resources.getQuantityString(
            MR.plurals.notification_checkout_places.resourceId,
            digest.placesCount,
            digest.placesCount,
        )
        return "$rooms · $places"
    }

    private companion object {
        const val WORK_TAG = "checkout_notification"
        const val WORK_NAME_PREFIX = "checkout_notification_"
    }
}
