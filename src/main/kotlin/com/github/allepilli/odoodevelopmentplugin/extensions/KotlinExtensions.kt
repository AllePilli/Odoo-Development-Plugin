package com.github.allepilli.odoodevelopmentplugin.extensions

/**
 * Removes all items that produce duplicate keys using the [uniqueKeySelector]
 */
fun <T, K> Iterable<T>.uniqueBy(uniqueKeySelector: (T) -> K): List<T> = groupBy(uniqueKeySelector)
        .values
        .map { it.first() }

fun <T> Collection<T>.takeUnlessEmpty() = takeUnless { it.isEmpty() }