package space.jamestang.ktimer

import org.slf4j.event.Level
import space.jamestang.ktimer.core.Constant
import space.jamestang.ktimer.core.KTimerServer


fun main() {

    Constant.initialize()
    Constant.logger.atLevel(Level.DEBUG)

    val server = KTimerServer(4396)
    server.start()

}