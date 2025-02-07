package com.github.allepilli.odoodevelopmentplugin.xml.dom.odoo_data_file.converters

import com.intellij.util.xml.converters.values.NumberValueConverter

class FloatConverter: NumberValueConverter<Float>(Float::class.java, false)