package dev.nonoxy.residetrack.common.utils.mapper

interface Mapper<From, To> {
    fun map(item: From): To

    fun map(list: List<From>): List<To> = list.mapNotNull { item ->
        map(item = item)
    }
}
