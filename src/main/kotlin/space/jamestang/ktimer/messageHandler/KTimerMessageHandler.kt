package space.jamestang.ktimer.messageHandler

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.LoggerFactory
import space.jamestang.ktimer.connectionManager.ConnectionManager
import space.jamestang.ktimer.message.AckData
import space.jamestang.ktimer.message.AckStatus
import space.jamestang.ktimer.message.ClientMetadata
import space.jamestang.ktimer.message.ClientRegisterData
import space.jamestang.ktimer.message.ErrorData
import space.jamestang.ktimer.message.HeartbeatData
import space.jamestang.ktimer.message.KTimerMessage
import space.jamestang.ktimer.message.MessageBuilder
import space.jamestang.ktimer.message.MessageData
import space.jamestang.ktimer.message.SystemInfo
import space.jamestang.ktimer.message.TimerCallbackData
import space.jamestang.ktimer.message.TimerCancelData
import space.jamestang.ktimer.message.TimerRegisterData
import space.jamestang.ktimer.message.enums.MessageType
import space.jamestang.ktimer.message.enums.TimerPriority

@ChannelHandler.Sharable
class TimerMessageHandler(private val connectionManager: ConnectionManager) : SimpleChannelInboundHandler<KTimerMessage>() {

    private val logger = LoggerFactory.getLogger(TimerMessageHandler::class.java)

    override fun channelActive(ctx: ChannelHandlerContext) {
        val connectionId = connectionManager.registerConnection(ctx.channel())
        logger.info("Channel active: ${ctx.channel().remoteAddress()}, connectionId: $connectionId")
        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val connectionId = ctx.channel().attr(ConnectionManager.CONNECTION_ID_KEY).get()
        if (connectionId != null) {
            connectionManager.removeConnection(connectionId)
            logger.info("Channel inactive: ${ctx.channel().remoteAddress()}, connectionId: $connectionId")
        }
        super.channelInactive(ctx)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: KTimerMessage) {
        val connectionId = ctx.channel().attr(ConnectionManager.CONNECTION_ID_KEY).get()
        if (connectionId == null) {
            logger.error("No connection ID found for channel: ${ctx.channel().remoteAddress()}")
            return
        }

        try {
            when (msg.type) {
                MessageType.CLIENT_REGISTER -> handleClientRegister(ctx, msg, connectionId)
                MessageType.CLIENT_UNREGISTER -> handleClientUnregister(ctx, msg, connectionId)
                MessageType.TIMER_REGISTER -> handleTimerRegister(ctx, msg, connectionId)
                MessageType.TIMER_CANCEL -> handleTimerCancel(ctx, msg, connectionId)
                MessageType.HEARTBEAT -> handleHeartbeat(ctx, msg, connectionId)
                MessageType.ACK -> handleAck(ctx, msg, connectionId)
                else -> {
                    logger.warn("Unsupported message type: ${msg.type}")
                    sendError(ctx, msg, "UNSUPPORTED_MESSAGE_TYPE", "Message type ${msg.type} is not supported")
                }
            }
        } catch (e: Exception) {
            logger.error("Error handling message: $msg", e)
            sendError(ctx, msg, "INTERNAL_ERROR", "Internal server error: ${e.message}")
        }
    }

    private fun handleClientRegister(ctx: ChannelHandlerContext, msg: KTimerMessage, connectionId: String) {
        val registerData = msg.getDataAs<ClientRegisterData>()
        if (registerData == null) {
            sendError(ctx, msg, "INVALID_DATA", "Invalid client register data")
            return
        }

        val success = connectionManager.bindClientId(connectionId, msg.clientId)
        if (success) {
            val ackMessage = MessageBuilder.createAck(
                clientId = msg.clientId,
                originalMessageId = msg.messageId,
                message = "Client registered successfully",
                details = mapOf(
                    "connectionId" to connectionId,
                    "registeredAt" to System.currentTimeMillis()
                )
            )
            ctx.writeAndFlush(ackMessage)
            logger.info("Client registered: ${msg.clientId}")
        } else {
            sendError(ctx, msg, "REGISTRATION_FAILED", "Failed to register client")
        }
    }

    private fun handleClientUnregister(ctx: ChannelHandlerContext, msg: KTimerMessage, connectionId: String) {
        connectionManager.removeConnection(connectionId)
        val ackMessage = MessageBuilder.createAck(
            clientId = msg.clientId,
            originalMessageId = msg.messageId,
            message = "Client unregistered successfully"
        )
        ctx.writeAndFlush(ackMessage).addListener(ChannelFutureListener.CLOSE)
    }

    private fun handleTimerRegister(ctx: ChannelHandlerContext, msg: KTimerMessage, connectionId: String) {
        val timerData = msg.getDataAs<TimerRegisterData>()
        if (timerData == null) {
            sendError(ctx, msg, "INVALID_DATA", "Invalid timer register data")
            return
        }

        // TODO: 实现定时器注册逻辑
        // timerService.registerTimer(msg.clientId, timerData)

        val ackMessage = MessageBuilder.createAck(
            clientId = msg.clientId,
            originalMessageId = msg.messageId,
            message = "Timer registered successfully",
            details = mapOf(
                "timerId" to timerData.timerId,
                "executeAt" to (System.currentTimeMillis() + timerData.delayMillis)
            )
        )
        ctx.writeAndFlush(ackMessage)
        logger.info("Timer registered: ${timerData.timerId} for client ${msg.clientId}")
    }

    private fun handleTimerCancel(ctx: ChannelHandlerContext, msg: KTimerMessage, connectionId: String) {
        val cancelData = msg.getDataAs<TimerCancelData>()
        if (cancelData == null) {
            sendError(ctx, msg, "INVALID_DATA", "Invalid timer cancel data")
            return
        }

        // TODO: 实现定时器取消逻辑
        // timerService.cancelTimer(msg.clientId, cancelData.timerId)

        val ackMessage = MessageBuilder.createAck(
            clientId = msg.clientId,
            originalMessageId = msg.messageId,
            message = "Timer cancelled successfully",
            details = mapOf("timerId" to cancelData.timerId)
        )
        ctx.writeAndFlush(ackMessage)
        logger.info("Timer cancelled: ${cancelData.timerId} for client ${msg.clientId}")
    }

    private fun handleHeartbeat(ctx: ChannelHandlerContext, msg: KTimerMessage, connectionId: String) {
        connectionManager.updateHeartbeat(connectionId)

        // 回复心跳确认
        val ackMessage = MessageBuilder.createAck(
            clientId = msg.clientId,
            originalMessageId = msg.messageId,
            message = "Heartbeat received"
        )
        ctx.writeAndFlush(ackMessage)
    }

    private fun handleAck(ctx: ChannelHandlerContext, msg: KTimerMessage, connectionId: String) {
        val ackData = msg.getDataAs<AckData>()
        logger.debug("Received ACK for message: ${ackData?.originalMessageId} from client: ${msg.clientId}")
        // TODO: 处理确认消息，更新消息状态
    }

    private fun sendError(ctx: ChannelHandlerContext, originalMsg: KTimerMessage, errorCode: String, errorMessage: String) {
        val errorMsg = MessageBuilder.createError(
            clientId = originalMsg.clientId,
            originalMessageId = originalMsg.messageId,
            errorCode = errorCode,
            errorMessage = errorMessage
        )
        ctx.writeAndFlush(errorMsg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("Exception in channel: ${ctx.channel().remoteAddress()}", cause)
        ctx.close()
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : MessageData> KTimerMessage.getDataAs(): T? {
        return when (data) {
            is T -> data
            is Map<*, *> -> {

                try {
                    convertMapToMessageData<T>(data as Map<String, Any>)
                } catch (e: Exception) {
                    null
                }
            }

            else -> null
        }
    }

    private inline fun <reified T : MessageData> convertMapToMessageData(map: Map<String, Any>): T? {
        return when (T::class) {
            ClientRegisterData::class -> convertToClientRegisterData(map) as? T
            TimerRegisterData::class -> convertToTimerRegisterData(map) as? T
            TimerCancelData::class -> convertToTimerCancelData(map) as? T
            TimerCallbackData::class -> convertToTimerCallbackData(map) as? T
            HeartbeatData::class -> convertToHeartbeatData(map) as? T
            AckData::class -> convertToAckData(map) as? T
            ErrorData::class -> convertToErrorData(map) as? T
            else -> null
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun convertToClientRegisterData(map: Map<String, Any>): ClientRegisterData? {
        return try {
            val metadataMap = map["metadata"] as? Map<String, Any> ?: return null
            ClientRegisterData(
                instanceId = map["instanceId"] as String,
                serviceName = map["serviceName"] as String,
                version = map["version"] as String,
                capabilities = (map["capabilities"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                metadata = ClientMetadata(
                    hostname = metadataMap["hostname"] as String,
                    ip = metadataMap["ip"] as String,
                    port = (metadataMap["port"] as Number).toInt(),
                    environment = metadataMap["environment"] as String
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertToTimerRegisterData(map: Map<String, Any>): TimerRegisterData? {
        return try {
            TimerRegisterData(
                timerId = map["timerId"] as String,
                delayMillis = (map["delayMillis"] as Number).toLong(),
                executeAt = (map["executeAt"] as? Number)?.toLong(),
                repeatInterval = (map["repeatInterval"] as? Number)?.toLong() ?: 0L,
                maxRetries = (map["maxRetries"] as? Number)?.toInt() ?: 3,
                priority = TimerPriority.valueOf(map["priority"] as? String ?: "NORMAL"),
                payload = map["payload"] ?: Any(),
                tags = (map["tags"] as? Map<String, Any>)?.mapValues { it.value.toString() }
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun convertToTimerCancelData(map: Map<String, Any>): TimerCancelData? {
        return try {
            TimerCancelData(
                timerId = map["timerId"] as String,
                reason = map["reason"] as? String,
                force = map["force"] as? Boolean ?: false
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun convertToTimerCallbackData(map: Map<String, Any>): TimerCallbackData? {
        return try {
            TimerCallbackData(
                timerId = map["timerId"] as String,
                originalTimestamp = (map["originalTimestamp"] as Number).toLong(),
                executeTimestamp = (map["executeTimestamp"] as Number).toLong(),
                attempt = (map["attempt"] as? Number)?.toInt() ?: 1,
                payload = map["payload"] ?: Any()
            )
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertToHeartbeatData(map: Map<String, Any>): HeartbeatData? {
        return try {
            val systemInfoMap = map["systemInfo"] as? Map<String, Any>
            val systemInfo = systemInfoMap?.let {
                SystemInfo(
                    cpuUsage = (it["cpuUsage"] as Number).toDouble(),
                    memoryUsage = (it["memoryUsage"] as Number).toDouble(),
                    diskUsage = (it["diskUsage"] as Number).toDouble()
                )
            }

            HeartbeatData(
                status = map["status"] as? String ?: "healthy",
                activeTimers = (map["activeTimers"] as? Number)?.toInt() ?: 0,
                processedCount = (map["processedCount"] as? Number)?.toLong() ?: 0L,
                uptime = (map["uptime"] as Number).toLong(),
                systemInfo = systemInfo
            )
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertToAckData(map: Map<String, Any>): AckData? {
        return try {
            AckData(
                originalMessageId = map["originalMessageId"] as String,
                status = AckStatus.valueOf(map["status"] as? String ?: "SUCCESS"),
                code = (map["code"] as? Number)?.toInt() ?: 200,
                message = map["message"] as String,
                details = map["details"] as? Map<String, Any>
            )
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertToErrorData(map: Map<String, Any>): ErrorData? {
        return try {
            ErrorData(
                originalMessageId = map["originalMessageId"] as? String,
                errorCode = map["errorCode"] as String,
                errorMessage = map["errorMessage"] as String,
                details = map["details"] as? Map<String, Any>,
                suggestions = (map["suggestions"] as? List<*>)?.filterIsInstance<String>()
            )
        } catch (_: Exception) {
            null
        }
    }
}