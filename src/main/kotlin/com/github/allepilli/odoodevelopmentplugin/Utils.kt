package com.github.allepilli.odoodevelopmentplugin

inline fun <reified E> buildArray(builderAction: MutableList<E>.() -> Unit): Array<E> = buildList(builderAction).toTypedArray()