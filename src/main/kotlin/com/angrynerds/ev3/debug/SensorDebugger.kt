package com.angrynerds.ev3.debug

import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.extensions.measureDistance
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlin.concurrent.thread

class SensorDebugger : HttpHandler {
    override fun handle(exchange: HttpExchange?) {
        val autoRefreshScript = "<meta http-equiv=\"refresh\" content=\"2\" />"
        val response = "<html>$autoRefreshScript" +
                "<b>Infrared:</b> ${FeederRobot.infraredSensor.measureDistance()}<br/>" +
                "<b>Ultrasonic:</b> ${FeederRobot.ultrasonicSensor.measureDistance()}<br/>" +
                "<b>Right Color:</b> ${FeederRobot.colorSensorRight.sampleSize()}<br/>" +
                "<b>Forward Color:</b> ${FeederRobot.colorSensorForward.sampleSize()}" +
                "</html>"
        exchange?.sendResponseHeaders(200, response.length.toLong())
        val output = exchange?.responseBody
        output?.write(response.toByteArray())
        output?.close()
    }

    companion object {
        fun startDebugServer() {
            thread {
                val server = HttpServer.create(InetSocketAddress(8080), 0)
                server.createContext("/sensors", SensorDebugger())
                server.executor = null
                server.start()
            }
        }
    }
}