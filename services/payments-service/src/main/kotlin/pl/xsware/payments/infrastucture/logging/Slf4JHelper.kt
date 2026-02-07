package pl.xsware.payments.infrastucture.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <T : Any> T.logger(name: String? = null): Logger =
    name?.let { LoggerFactory.getLogger(it) }
        ?: LoggerFactory.getLogger(this::class.java)
