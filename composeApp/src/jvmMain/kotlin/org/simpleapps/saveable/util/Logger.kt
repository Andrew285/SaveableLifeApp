package org.simpleapps.saveable.util

import org.slf4j.LoggerFactory

// call this anywhere to get a logger for that class
inline fun <reified T> T.logger() = LoggerFactory.getLogger(T::class.java)

