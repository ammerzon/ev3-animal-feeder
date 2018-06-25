package com.angrynerds.ev3.debug

import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Handler
import java.util.logging.LogRecord

class EV3LogHandler : Handler() {

    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    override fun publish(record: LogRecord) {
        val builder = StringBuilder()
        builder.append(dateFormat.format(Date(record.millis)))
                .append(" ")
                .append("[${record.loggerName}]")
                .append(" ")
                .append(record.level)
                .append(" ")
                .append(record.sourceMethodName)
                .append(" - ")
                .append(record.message)
        println(builder.toString())
    }

    override fun flush() {}

    @Throws(SecurityException::class)
    override fun close() {
    }
}