package dev.nonoxy.residetrack.core.notifications.ios

import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import dev.nonoxy.residetrack.core.notifications.domain.CheckoutDigest
import dev.nonoxy.residetrack.core.notifications.domain.LocalNotifier
import dev.nonoxy.residetrack.res.MR
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSTimeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

/**
 * Планирует локальные пуши о выезде через системный [UNUserNotificationCenter].
 *
 * Календарный триггер (год/месяц/день/час/минута) переживает перезапуск: локалки держит
 * системный демон, entitlement не требуется.
 */
internal class IosLocalNotifier : LocalNotifier {

    private val center: UNUserNotificationCenter
        get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun requestPermission(): Boolean = suspendCoroutine { continuation ->
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            continuation.resume(granted)
        }
    }

    override suspend fun sync(digests: List<CheckoutDigest>) {
        center.removeAllPendingNotificationRequests()

        digests.forEach { digest ->
            val content = UNMutableNotificationContent().apply {
                setTitle(buildTitle())
                setBody(buildBody(digest))
                setSound(UNNotificationSound.defaultSound)
            }

            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = "$IDENTIFIER_PREFIX${digest.date}",
                content = content,
                trigger = triggerFor(digest.fireAtEpochMillis),
            )
            center.addNotificationRequest(request, withCompletionHandler = null)
        }
    }

    private fun triggerFor(fireAtEpochMillis: Long): UNCalendarNotificationTrigger {
        val secondsSinceReferenceDate = fireAtEpochMillis / MILLIS_IN_SECOND - NSTimeIntervalSince1970
        val fireDate = NSDate(timeIntervalSinceReferenceDate = secondsSinceReferenceDate)
        val units = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
            NSCalendarUnitHour or NSCalendarUnitMinute
        val components = NSCalendar.currentCalendar.components(units, fromDate = fireDate)
        return UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = false)
    }

    private fun buildTitle(): String =
        MR.strings.notification_checkout_title.desc().localized()

    private fun buildBody(digest: CheckoutDigest): String {
        val rooms = MR.strings.notification_checkout_rooms
            .format(digest.roomNumbers.joinToString(separator = ", "))
            .localized()
        val places = MR.plurals.notification_checkout_places
            .format(digest.placesCount, digest.placesCount)
            .localized()
        return "$rooms · $places"
    }

    private companion object {
        const val IDENTIFIER_PREFIX = "checkout_notification_"
        const val MILLIS_IN_SECOND = 1000.0
    }
}
