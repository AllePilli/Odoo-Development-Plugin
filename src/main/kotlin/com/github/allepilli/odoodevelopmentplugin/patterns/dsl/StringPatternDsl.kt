package com.github.allepilli.odoodevelopmentplugin.patterns.dsl

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.StringPattern

class StringPatternDsl(var pattern: StringPattern) {
    var head: String? = null
    var tail: String? = null
    var contains: Any? = null
    var containsChars: String? = null
    var matches: String? = null
    var minLength: Int? = null
    var maxLength: Int? = null
    var length: Int? = null

    /**
     * DO NOT CALL
     */
    fun _build(): StringPattern {
        head?.let { pattern = pattern.startsWith(it) }
        tail?.let { pattern = pattern.endsWith(it) }
        contains?.let {
            pattern = when (it) {
                is String -> pattern.contains(it)
                is ElementPattern<*> -> pattern.contains(it as ElementPattern<Char>)
                else -> throw IllegalStateException("contains should be ElementPattern<Char> or String")
            }
        }
        containsChars?.let { pattern = pattern.containsChars(it) }
        matches?.let { pattern = pattern.matches(it) }
        minLength?.let { pattern = pattern.longerThan(it) }
        maxLength?.let { pattern = pattern.shorterThan(it) }
        length?.let { pattern = pattern.withLength(it) }

        return pattern
    }

    fun oneOf(vararg values: String) { pattern = pattern.oneOf(*values) }
    fun oneOfIgnoreCase(vararg values: String) { pattern = pattern.oneOfIgnoreCase(*values) }
    fun oneOf(set: Collection<String>) { pattern = pattern.oneOf(set) }
}

fun string(init: StringPatternDsl.() -> Unit = {}): StringPattern {
    val stringPatternDsl = StringPatternDsl(StandardPatterns.string())
    stringPatternDsl.init()
    return stringPatternDsl._build()
}