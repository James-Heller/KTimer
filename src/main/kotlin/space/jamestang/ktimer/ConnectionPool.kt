package space.jamestang.ktimer

import io.netty.channel.ChannelHandlerContext
import space.jamestang.ktimer.core.Constant
import space.jamestang.ktimer.data.KTimerMessage
import java.util.concurrent.ConcurrentHashMap

class ConnectionPool {

    private val pool = ConcurrentHashMap<String, ChannelHandlerContext>()

    fun addConnection(id: String, channel: ChannelHandlerContext) {
        pool[id] = channel
    }

    fun getConnection(id: String): ChannelHandlerContext? {
        return pool[id]
    }

    fun removeConnection(id: String) {
        pool.remove(id)
    }

    fun postMessage(id: String, payload: KTimerMessage) {
        val channel = pool[id]
        if (channel == null) {
            Constant.logger.warn("Channel for client $id not found!")
            return
        }
        if (!channel.channel().isActive) {
            Constant.logger.warn("Channel for client $id is not active!")
            pool.remove(id)
            return
        }
        try {
            channel.writeAndFlush(payload)
            Constant.logger.info("Message sent to client $id successfully.")
        } catch (e: Exception) {
            Constant.logger.error("Failed to send message to client $id: ${e.message}")
        }
    }
}
