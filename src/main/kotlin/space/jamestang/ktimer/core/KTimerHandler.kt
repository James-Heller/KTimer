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

    @OptIn(ExperimentalUuidApi::class)
    private fun handleClientRegister(ctx: ChannelHandlerContext, msg: KTimerMessage) {

        if (connectionPool.getConnection(msg.clientId) != null) {
            Constant.logger.warn("Client ${msg.clientId} was already registered!")
            ctx.writeAndFlush(KTimerMessage(msg.clientId, KTimerMessage.MessageType.ERROR, "Already existed!!!", null))
            ctx.close()
            return
        }

        val clientId = Uuid.random().toString()
        if (registerClient(clientId, ctx) == -1) {
            Constant.logger.warn("Client $clientId was already registered!")
            ctx.writeAndFlush(KTimerMessage(clientId, KTimerMessage.MessageType.ERROR, "Already existed!!!", null))
            ctx.close()
            return
        }
        val response = KTimerMessage.response(clientId)
        ctx.writeAndFlush(response)
    }

    private fun handleScheduleTask(ctx: ChannelHandlerContext, msg: KTimerMessage) {
        val clientId = msg.clientId
        val taskId = msg.taskId
        val context = msg.context

        if (connectionPool.getConnection(clientId) == null) {
            Constant.logger.warn("Client $clientId was not registered!")
            ctx.writeAndFlush(KTimerMessage(clientId, KTimerMessage.MessageType.ERROR, "Not registered!!!", null))
            ctx.close()
            return
        }

        if (context == null) {
            Constant.logger.warn("Task context is null!")
            ctx.writeAndFlush(KTimerMessage(clientId, KTimerMessage.MessageType.ERROR, "Task context is null!", null))
            ctx.close()
            return
        }

        if (taskId.isEmpty()) {
            Constant.logger.warn("Task ID is empty!")
            ctx.writeAndFlush(KTimerMessage(clientId, KTimerMessage.MessageType.ERROR, "Task ID is empty!", null))
            ctx.close()
            return
        }

        TaskPool.scheduleTask(taskId, context.delay){
            Constant.logger.info("Task $taskId was triggered!")
            val msg = KTimerMessage(clientId, KTimerMessage.MessageType.TASK_TRIGGER, taskId, context)

            connectionPool.postMessage(clientId, msg)
        }
    }

    private fun registerClient(clientId: String, ctx: ChannelHandlerContext): Int {
        if (connectionPool.getConnection(clientId) != null) {
            return -1
        }
        connectionPool.addConnection(clientId, ctx.channel())
        Constant.logger.info("Client $clientId was registered!")

        return 0
    }
}