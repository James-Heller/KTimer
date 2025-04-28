package space.jamestang.ktimer

import org.slf4j.event.Level
import space.jamestang.ktimer.core.Constant
import space.jamestang.ktimer.core.KTimerServer
import space.jamestang.ktimer.transactions.TaskPool


fun main() {

    Constant.initialize()
    Constant.logger.atLevel(Level.DEBUG)
    TaskPool.initialize()

    val server = KTimerServer(4396)
    server.start()

}