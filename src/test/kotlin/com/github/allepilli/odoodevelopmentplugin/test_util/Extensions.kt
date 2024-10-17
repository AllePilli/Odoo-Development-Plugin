package com.github.allepilli.odoodevelopmentplugin.test_util

fun <T: Any> T?.assertNotNull(lazyMessage: () -> String): T {
    assert(this != null, lazyMessage)
    return this!!
}