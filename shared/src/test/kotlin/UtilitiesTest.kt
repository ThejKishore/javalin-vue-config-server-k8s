package com.tk.learn.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class PrinterTest {

    @Test
    fun testMessage() {
        val message = "message"
        val testPrinter = Printer(message)
        assertEquals(testPrinter.message, message)
    }
}