package space.jamestang.ktimer

import io.netty.channel.Channel
import space.jamestang.ktimer.core.Constant
import java.util.concurrent.ConcurrentHashMap

class ConnectionPool {

    private val pool = ConcurrentHashMap<String, Channel>()

    fun addConnection(id: String, channel: Channel) {
        pool[id] = channel
        Constant.logger.info("Channel $id was registered!")
    }

    fun getConnection(id: String): Channel? {
        return pool[id]
    }

    fun removeConnection(id: String) {
        pool.remove(id)
        Constant.logger.info("Channel $id was removed!")
    }

    fun postMessage(id: String, payload: ByteArray){
        val channel = pool[id]
        if (channel != null && channel.isActive) {
            channel.writeAndFlush(payload)
        } else {
            Constant.logger.warn("Channel $id was not registered or is offline!")
        }
    }
}