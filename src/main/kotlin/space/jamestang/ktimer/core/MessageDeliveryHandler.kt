package space.jamestang.ktimer.core

import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import mu.KotlinLogging
import space.jamestang.ktimer.enum.KTimerMessageType
import space.jamestang.ktimer.message.KTimerMessage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MessageDeliveryHandler(private val clientRegistry: ClientRegistry): AutoCloseable {

    companion object{
        private val logger = KotlinLogging.logger {  }
    }


    private val timer = HashedWheelTimer(10, TimeUnit.MILLISECONDS, 512)
    private val pendingTasks = ConcurrentHashMap<String, MutableList<TimerTaskEntry>>()

    fun <T> scheduleMessage(clientId: String, delay: Long, message: KTimerMessage<T>){
        val task = TimerTask { deliverMessage(clientId, message.payload) }

        val timeout = timer.newTimeout(task, delay, TimeUnit.SECONDS)
        val taskEntry = TimerTaskEntry(clientId, message.messageId, timeout)
        pendingTasks.computeIfAbsent(clientId){ mutableListOf() }.add(taskEntry)
        logger.info{ "Scheduled message ${message.messageId}" }

    }


    fun cancelMessage(clientId: String, messageId: String) {
        pendingTasks[clientId]?.removeIf { it.messageId == messageId }
        if (pendingTasks[clientId]?.isEmpty() == true){
            pendingTasks.remove(clientId)
        }
    }

    override fun close() {
        timer.stop()
        pendingTasks.clear()
        logger.info { "MessageDeliveryHandler closed and all tasks cleared." }
    }

    private fun <T> deliverMessage(clientId: String, payload: T) {
        val channel = clientRegistry.getChannel(clientId)
        if (channel != null && channel.isActive) {
            val message = KTimerMessage(
                clientId = clientId,
                messageId = "msg-${System.currentTimeMillis()}",
                type = KTimerMessageType.TASK_TRIGGER,
                unit = null,
                payload = payload
            )
            channel.writeAndFlush(message).addListener { future ->
                if (!future.isSuccess) {
                    logger.error { "Failed to deliver message to client $clientId: ${future.cause()}" }
                } else {
                    logger.info { "Message delivered to client $clientId successfully." }
                }
            }
        } else {
            logger.warn { "Client $clientId is not connected or does not exist." }
            TODO("Add retry logic")
        }
    }






    data class TimerTaskEntry(
        val clientId: String,
        val messageId: String,
        val timeout: Timeout
    )
}