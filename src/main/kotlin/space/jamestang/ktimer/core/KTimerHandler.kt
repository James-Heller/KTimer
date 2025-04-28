package space.jamestang.ktimer.core

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import space.jamestang.ktimer.ConnectionPool
import space.jamestang.ktimer.data.KTimerMessage
import space.jamestang.ktimer.transactions.TaskPool
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class KTimerHandler(private val connectionPool: ConnectionPool) : SimpleChannelInboundHandler<KTimerMessage>() {


    @OptIn(ExperimentalUuidApi::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: KTimerMessage) {
        // Handle the incoming message
        when (msg.type) {
            KTimerMessage.MessageType.CLIENT_REGISTER -> handleClientRegister(ctx, msg)
            KTimerMessage.MessageType.SCHEDULE_TASK -> handleScheduleTask(ctx, msg)
            else -> TODO()
        }

    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        // Handle exceptions
        cause?.let {
            Constant.logger.error("Exception caught: ${it.message}")
            ctx?.close()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleClientRegister(ctx: ChannelHandlerContext, msg: KTimerMessage) {
        val clientId = msg.clientId ?: Uuid.random().toString()

        if (connectionPool.getConnection(clientId) != null) {
            sendErrorAndClose(ctx, clientId, "Already registered!")
            return
        }

        if (registerClient(clientId, ctx)) {
            val response = KTimerMessage.response(clientId)
            ctx.writeAndFlush(response)
        } else {
            sendErrorAndClose(ctx, clientId, "Registration failed!")
        }
    }

    private fun handleScheduleTask(ctx: ChannelHandlerContext, msg: KTimerMessage) {
        val clientId = msg.clientId
        val taskId = msg.taskId
        val context = msg.context

        if (clientId == null || connectionPool.getConnection(clientId) == null) {
            sendErrorAndClose(ctx, clientId, "Client not registered!")
            return
        }

        if (taskId.isEmpty() || context == null) {
            sendErrorAndClose(ctx, clientId, "Invalid task details!")
            return
        }

        TaskPool.scheduleTask(taskId, context.delay) {
            Constant.logger.info("Task $taskId was triggered!")
            val taskMessage = KTimerMessage(clientId, KTimerMessage.MessageType.TASK_TRIGGER, taskId, context)
            connectionPool.postMessage(clientId, taskMessage)
        }
    }

    private fun registerClient(clientId: String, ctx: ChannelHandlerContext): Boolean {
        return if (connectionPool.getConnection(clientId) == null) {
            connectionPool.addConnection(clientId, ctx.channel())
            Constant.logger.info("Client $clientId was registered!")
            true
        } else {
            false
        }
    }

    private fun sendErrorAndClose(ctx: ChannelHandlerContext, clientId: String?, errorMessage: String) {
        Constant.logger.warn("Error for client $clientId: $errorMessage")
        ctx.writeAndFlush(KTimerMessage(clientId, KTimerMessage.MessageType.ERROR, errorMessage, null))
        ctx.close()
    }
}
