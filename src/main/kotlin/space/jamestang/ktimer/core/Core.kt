package space.jamestang.ktimer.core

import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import org.slf4j.LoggerFactory
import space.jamestang.ktimer.connectionManager.ConnectionManager
import space.jamestang.ktimer.message.MessageBuilder
import space.jamestang.ktimer.message.TimerRegisterData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class Core(
    private val connectionManager: ConnectionManager
) {
    private val timer = HashedWheelTimer()
    private val logger = LoggerFactory.getLogger(Core::class.java)
    private val tasks = ConcurrentHashMap<String, ConcurrentHashMap<String, Timeout>>()


    /**
     * Registers a task to be executed by the timer.
     *
     * @param clientId The ID of the client associated with the task.
     * @param timerData The data for the timer, including the delay, maximum retries, and payload.
     *
     * The method checks if a task with the same ID is already registered for the client. If not, it schedules the task
     * to be executed after the specified delay. Upon execution, the task sends a callback to the client
     * with the timer information. If the client's connection is inactive, the task is aborted. The
     * callback sending is retried up to the maximum number of retries defined in the timer data.
     */
    fun registerTask(clientId: String, timerData: TimerRegisterData) {
        val clientTasks = tasks.computeIfAbsent(clientId) { ConcurrentHashMap() }

        if (clientTasks[timerData.timerId] != null) {
            logger.warn("Task with ID ${timerData.timerId} is already registered for client $clientId, skipping registration")
            return
        }

        val now = System.currentTimeMillis()
        scheduleTask(timerData.delayMillis, TimeUnit.MILLISECONDS) {
            try {
                val connection = connectionManager.getConnectionByClientId(clientId)

                if (connection == null || !connection.channel.isActive) {
                    logger.warn("Connection for client $clientId is not active, cannot send timer callback")
                    return@scheduleTask
                }

                for (times in 1..timerData.maxRetries) {
                    val data = MessageBuilder.createTimerCallback(clientId, timerData.timerId, now, timerData.payload, classInfo = timerData.classInfo, attempt = times)
                    if (connectionManager.sendToClient(clientId, data)) {
                        logger.info("Timer callback sent to client $clientId for task ${timerData.timerId} on attempt $times")
                        clientTasks.remove(timerData.timerId)
                        break
                    }
                }
            } catch (e: Exception) {
                logger.error("Error executing timer task for client $clientId", e)
            }
        }.also { clientTasks[timerData.timerId] = it }
    }

    /**
     * Cancels a registered task.
     *
     * @param clientId The ID of the client associated with the task.
     * @param taskId The ID of the task to be canceled.
     *
     * The method retrieves the task by its ID for the specified client and cancels it if found. If no task is found with the
     * given ID for the client, a warning is logged.
     */
    fun cancelTask(clientId: String, taskId: String) {
        tasks[clientId]?.let { clientTasks ->
            clientTasks[taskId]?.let {
                clientTasks.remove(taskId)?.cancel()
            } ?: logger.warn("No task found with ID: $taskId for client: $clientId")
        } ?: logger.warn("No tasks found for client: $clientId")
    }


    private fun scheduleTask(delay: Long, unit: TimeUnit, action: () -> Unit): Timeout {
        return timer.newTimeout({ timeout ->
            try {
                action()
            } catch (e: Exception) {
                logger.error("Error executing scheduled task", e)
            }
        }, delay, unit)
    }

}