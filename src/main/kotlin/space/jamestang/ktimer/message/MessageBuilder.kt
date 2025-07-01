package space.jamestang.ktimer.message

import space.jamestang.ktimer.message.enums.MessageType
import space.jamestang.ktimer.message.enums.TimerPriority

/**
 * 消息构建器
 */
/**
 * 消息构建器
 */
object MessageBuilder {

    /**
     * 创建客户端注册消息
     */
    fun createClientRegister(
        clientId: String,
        instanceId: String,
        serviceName: String,
        version: String,
        metadata: ClientMetadata
    ): KTimerMessage {
        return KTimerMessage(
            type = MessageType.CLIENT_REGISTER,
            messageId = generateMessageId(),
            clientId = clientId,
            data = ClientRegisterData(
                instanceId = instanceId,
                serviceName = serviceName,
                version = version,
                capabilities = listOf("timer", "callback"),
                metadata = metadata
            ),
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 创建定时器注册消息
     */
    fun createTimerRegister(
        clientId: String,
        timerId: String,
        delayMillis: Long,
        payload: Any,
        priority: TimerPriority = TimerPriority.NORMAL,
        tags: Map<String, String>? = null
    ): KTimerMessage {
        return KTimerMessage(
            type = MessageType.TIMER_REGISTER,
            messageId = generateMessageId(),
            clientId = clientId,
            data = TimerRegisterData(
                timerId = timerId,
                delayMillis = delayMillis,
                executeAt = System.currentTimeMillis() + delayMillis,
                payload = payload,
                priority = priority,
                tags = tags
            ),
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 创建定时器取消消息
     */
    fun createTimerCancel(
        clientId: String,
        timerId: String,
        reason: String? = null
    ): KTimerMessage {
        return KTimerMessage(
            type = MessageType.TIMER_CANCEL,
            messageId = generateMessageId(),
            clientId = clientId,
            data = TimerCancelData(
                timerId = timerId,
                reason = reason
            ),
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 创建定时器回调消息
     */
    fun createTimerCallback(
        clientId: String,
        timerId: String,
        originalTimestamp: Long,
        payload: Any,
        attempt: Int = 1
    ): KTimerMessage {
        return KTimerMessage(
            type = MessageType.TIMER_CALLBACK,
            messageId = generateMessageId(),
            clientId = clientId,
            data = TimerCallbackData(
                timerId = timerId,
                originalTimestamp = originalTimestamp,
                executeTimestamp = System.currentTimeMillis(),
                attempt = attempt,
                payload = payload
            ),
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 创建心跳消息
     */
    fun createHeartbeat(
        clientId: String,
        activeTimers: Int = 0,
        processedCount: Long = 0L,
        uptime: Long,
        systemInfo: SystemInfo? = null
    ): KTimerMessage {
        return KTimerMessage(
            type = MessageType.HEARTBEAT,
            messageId = generateMessageId(),
            clientId = clientId,
            data = HeartbeatData(
                activeTimers = activeTimers,
                processedCount = processedCount,
                uptime = uptime,
                systemInfo = systemInfo
            ),
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 创建确认消息
     */
    fun createAck(
        clientId: String,
        originalMessageId: String,
        status: AckStatus = AckStatus.SUCCESS,
        message: String = "Success",
        details: Map<String, Any>? = null
    ): KTimerMessage {
        return KTimerMessage(
            type = MessageType.ACK,
            messageId = generateMessageId(),
            clientId = clientId,
            data = AckData(
                originalMessageId = originalMessageId,
                status = status,
                message = message,
                details = details
            ),
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 创建错误消息
     */
    fun createError(
        clientId: String,
        originalMessageId: String? = null,
        errorCode: String,
        errorMessage: String,
        details: Map<String, Any>? = null,
        suggestions: List<String>? = null
    ): KTimerMessage {
        return KTimerMessage(
            type = MessageType.ERROR,
            messageId = generateMessageId(),
            clientId = clientId,
            data = ErrorData(
                originalMessageId = originalMessageId,
                errorCode = errorCode,
                errorMessage = errorMessage,
                details = details,
                suggestions = suggestions
            ),
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 生成消息ID
     */
    private fun generateMessageId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "msg_${timestamp}_${random}"
    }
}