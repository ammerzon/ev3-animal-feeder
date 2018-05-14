package com.angrynerds.ev3.debug

import com.angrynerds.ev3.core.FeederRobot
import com.angrynerds.ev3.extensions.getDistance
import com.angrynerds.ev3.extensions.htmlColor
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlin.concurrent.thread

/**
 * This class starts a web server on port 8081 to display the current sensor values.
 */
class SensorDebugger : HttpHandler {
    override fun handle(exchange: HttpExchange?) {
        val autoRefreshScript = "<html>" +
                "<head>" +
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.slim.min.js\"></script>" +
                "<script type=\"text/javascript\"> var timeout; \$(document).ready(function () { startReload(); }); function stopReload() { clearTimeout(timeout); } function startReload() { timeout = setTimeout(function() { window.location.reload(); }, 2000); } </script></head>"
        val response = autoRefreshScript +
                "<body>" +
                "<b>Infrared:</b> ${FeederRobot.infraredSensor.getDistance()}<br/>" +
                "<b>Ultrasonic:</b> ${FeederRobot.ultrasonicSensor.getDistance()}<br/>" +
                "<b style=\"color:${FeederRobot.colorSensorRight.htmlColor()}\">Right Color: </b>${FeederRobot.colorSensorRight.colorID} <br/>" +
                "<b style=\"color:${FeederRobot.colorSensorForward.htmlColor()}\">Forward Color: </b>${FeederRobot.colorSensorForward.colorID}" +
                "<br /><button onclick=\"stopReload();\">Stop auto refresh!</button>" +
                "<button onclick=\"startReload();\">Start auto refresh!</button>" +
                "</body></html>"
        exchange?.sendResponseHeaders(200, response.length.toLong())
        val output = exchange?.responseBody
        output?.write(response.toByteArray())
        output?.close()
    }

    companion object {
        fun startDebugServer() {
            thread {
                val port = 8081

                val server = HttpServer.create(InetSocketAddress(port), 0)
                server.createContext("/sensors", SensorDebugger())
                server.executor = null
                server.start()
                println("Server startet on port $port")
            }
        }
    }
}