package space.jamestang.ktimer.core

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import space.jamestang.ktimer.enum.KTimerMessageType
import space.jamestang.ktimer.message.KTimerMessage

class KTimerMessageHandler: SimpleChannelInboundHandler<KTimerMessage<Any>>() {

    companion object{
        private val logger = mu.KotlinLogging.logger {}
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: KTimerMessage<Any>) {

        val response = when(msg.type){
            KTimerMessageType.HEARTBEAT -> KTimerMsgProcessor.processHeartBeat(msg)
            KTimerMessageType.APPLY_CODE -> KTimerMsgProcessor.processApplyCode(msg)
            KTimerMessageType.TASK_SEND -> TODO()
            KTimerMessageType.TASK_TRIGGER -> TODO()
            KTimerMessageType.REGISTER -> TODO()
            KTimerMessageType.UNREGISTER -> TODO()
            KTimerMessageType.ERROR -> TODO()
            else -> {
                KTimerMessage(
                    clientId = msg.clientId,
                    messageId = msg.messageId,
                    type = KTimerMessageType.ERROR,
                    unit = null,
                    payload = Unit
                )
            }
        }

        ctx.writeAndFlush(response).addListener { future ->
            if (!future.isSuccess) {
                logger.error { "Failed to send response: ${future.cause()?.message}" }
            } else {
                logger.info { "Response sent successfully: ${response.type}" }
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        val errorMessage = KTimerMessage<Unit>(
            clientId = "server",
            messageId = "error",
            type = KTimerMessageType.ERROR,
            unit = null,
            payload = Unit
        )

        logger.error(cause) { "Exception caught: ${cause.message}" }
        ctx.writeAndFlush(errorMessage).addListener { future ->
            if (!future.isSuccess) {
                logger.error { "Failed to send error message: ${future.cause()?.message}" }
            }
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info { "Channel inactive" }
        ctx.close()
    }
}