package dev.nonoxy.residetrack.common.utils

fun String.isDigitsOnly(): Boolean {
    forEach { char -> if (!char.isDigit()) return false }
    return true
}
