package dev.nonoxy.residetrack.core.notifications.android

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import dev.nonoxy.residetrack.res.MR

internal object NotificationChannels {

    const val CHECKOUT_CHANNEL_ID = "checkout_reminders"

    /** Идемпотентно создаёт канал напоминаний о выезде; повторный вызов безопасен. */
    fun ensureCheckoutChannel(context: Context) {
        val channel = NotificationChannelCompat.Builder(
            CHECKOUT_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        )
            .setName(context.getString(MR.strings.notification_channel_checkout_name.resourceId))
            .setDescription(context.getString(MR.strings.notification_channel_checkout_description.resourceId))
            .build()

        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}
