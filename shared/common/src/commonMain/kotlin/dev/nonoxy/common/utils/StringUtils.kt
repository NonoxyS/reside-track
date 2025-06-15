package dev.nonoxy.common.utils

fun String.isDigitsOnly(): Boolean {
    forEach { char -> if (!char.isDigit()) return false }
    return true
}

fun String.isDecemicalNumber(): Boolean {
    forEach { char ->
        if (!char.isDigit() && (char != '.' && char != ',')) {
            return false
        }
    }
    return true
}
