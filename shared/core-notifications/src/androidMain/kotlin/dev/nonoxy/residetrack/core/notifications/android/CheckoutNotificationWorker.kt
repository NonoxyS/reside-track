package dev.nonoxy.residetrack.core.notifications.android

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Показывает один заранее собранный пуш о выезде. Текст резолвится при планировании
 * (в [AndroidLocalNotifier]) и передаётся готовым — воркер лишь публикует уведомление.
 */
internal class CheckoutNotificationWorker(
    context: Context,
    params: WorkerParameters,
) : Worker(context, params) {

    override fun doWork(): Result {
        val notifier = NotificationManagerCompat.from(applicationContext)
        if (!notifier.areNotificationsEnabled()) return Result.success()

        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, 0)
        val title = inputData.getString(KEY_TITLE) ?: return Result.success()
        val body = inputData.getString(KEY_BODY).orEmpty()

        NotificationChannels.ensureCheckoutChannel(applicationContext)

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.CHECKOUT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()

        notifier.notify(notificationId, notification)
        return Result.success()
    }

    companion object {
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }
}
