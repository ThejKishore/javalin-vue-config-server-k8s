package com.tk.learn.utils

import kotlinx.serialization.Serializable

/**
 * Simple serializable printer class
 */
@Serializable
data class Printer(
    val message: String
)

