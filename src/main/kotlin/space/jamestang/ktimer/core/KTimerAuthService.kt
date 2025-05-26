package space.jamestang.ktimer.core

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import mu.KotlinLogging
import space.jamestang.ktimer.enum.KTimerMessageType
import space.jamestang.ktimer.message.KTimerMessage

@ChannelHandler.Sharable
class KTimerAuthService(private val registry: ClientRegistry): ChannelInboundHandlerAdapter() {

    companion object{
        private val logger = KotlinLogging.logger {  }

        val NO_AUTH_NEED_REQUESTS = listOf<KTimerMessageType>(KTimerMessageType.HEARTBEAT, KTimerMessageType.HEARTBEAT_RESPONSE,
            KTimerMessageType.AUTH_REQUEST, KTimerMessageType.APPLY_CODE)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is KTimerMessage<*>) {
            ctx.fireChannelRead(msg)
            return
        }

        when {
            isNoAuthNeeded(msg) -> handleNoAuthMessage(ctx, msg)
            registry.isRegistered(msg.clientId) -> ctx.fireChannelRead(msg)
            else -> handleUnauthorized(ctx, msg)
        }
    }

    private fun isNoAuthNeeded(msg: KTimerMessage<*>) =
        NO_AUTH_NEED_REQUESTS.contains(msg.type)

    private fun handleNoAuthMessage(ctx: ChannelHandlerContext, msg: KTimerMessage<*>) {
        if (msg.type == KTimerMessageType.AUTH_REQUEST) {
            registry.registerClient(msg.clientId, ctx.channel())
            val response = KTimerMessage(
                messageId = msg.messageId,
                type = KTimerMessageType.AUTH_SUCCESS,
                clientId = msg.clientId,
                unit = null,
                payload = "Authentication successful"
            )
            ctx.writeAndFlush(response)
        } else {
            ctx.fireChannelRead(msg)
        }
    }

    private fun handleUnauthorized(ctx: ChannelHandlerContext, msg: KTimerMessage<*>) {
        val response = KTimerMessage(
            messageId = msg.messageId,
            type = KTimerMessageType.ERROR,
            clientId = msg.clientId,
            unit = null,
            payload = "Unauthorized access"
        )
        ctx.writeAndFlush(response)
        logger.warn { "Unauthorized" }
    }

}
