package space.jamestang.ktimer.core

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import space.jamestang.ktimer.data.KTimerMessage
import space.jamestang.ktimer.transactions.TaskPool
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class KTimerHandler(private val connectionPool: ConnectionPool) : SimpleChannelInboundHandler<KTimerMessage>() {

    @OptIn(ExperimentalUuidApi::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: KTimerMessage) {
        when (msg.type) {
            KTimerMessage.MessageType.CLIENT_REGISTER -> handleClientRegister(ctx, msg)
            KTimerMessage.MessageType.SCHEDULE_TASK -> handleScheduleTask(ctx, msg)
            KTimerMessage.MessageType.CANCEL_TASK -> handleCancelTask(ctx, msg)
            else -> {
                Constant.logger.debug("Unhandled message: {}", msg)
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        Constant.logger.error("Exception caught: {}", cause.message)
        ctx.close()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleClientRegister(ctx: ChannelHandlerContext, msg: KTimerMessage) {
        val clientId = msg.clientId ?: Uuid.random().toString()
        registerClient(clientId, ctx)
        val response = KTimerMessage.response(clientId)
        ctx.writeAndFlush(response)
    }

    private fun handleScheduleTask(ctx: ChannelHandlerContext, msg: KTimerMessage) {
        val clientId = msg.clientId
        val taskId = msg.taskId
        val context = msg.context

        if (clientId.isNullOrEmpty()) {
            sendErrorAndClose(ctx, clientId, "Client not registered!")
            return
        }
        if (taskId.isEmpty() || context == null) {
            sendErrorAndClose(ctx, clientId, "Invalid task details!")
            return
        }

        if (connectionPool.getConnection(clientId) != null && connectionPool.getConnection(clientId) != ctx) {
            registerClient(clientId, ctx)
        }


        TaskPool.scheduleTask(taskId, context.delay) {
            Constant.logger.info("Task {} was triggered!", taskId)
            val taskMessage = KTimerMessage(clientId, KTimerMessage.MessageType.TASK_TRIGGER, taskId, context)
            connectionPool.postMessage(clientId, taskMessage)
        }
    }

    private fun handleCancelTask(ctx: ChannelHandlerContext, msg: KTimerMessage) {
        val clientId = msg.clientId
        val taskId = msg.taskId

        if (clientId.isNullOrEmpty()) {
            sendErrorAndClose(ctx, clientId, "Client not registered!")
            return
        }
        if (taskId.isEmpty()) {
            sendErrorAndClose(ctx, clientId, "Invalid task ID!")
            return
        }

        TaskPool.cancelTask(taskId)

        val cancelMessage = KTimerMessage(clientId, KTimerMessage.MessageType.TASK_CANCELLED, taskId, null)
        ctx.writeAndFlush(cancelMessage)
    }

    private fun registerClient(clientId: String, ctx: ChannelHandlerContext) {
        connectionPool.removeConnection(clientId)
        connectionPool.addConnection(clientId, ctx)
    }

    private fun sendErrorAndClose(ctx: ChannelHandlerContext, clientId: String?, errorMessage: String) {
        Constant.logger.warn("Error for client {}: {}", clientId, errorMessage)
        ctx.writeAndFlush(KTimerMessage(clientId, KTimerMessage.MessageType.ERROR, errorMessage, null))
        ctx.close()
    }
}