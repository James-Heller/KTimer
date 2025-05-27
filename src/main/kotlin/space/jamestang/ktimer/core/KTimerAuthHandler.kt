package space.jamestang.ktimer.core

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import mu.KotlinLogging
import space.jamestang.ktimer.enum.KTimerMessageType
import space.jamestang.ktimer.message.KTimerMessage

@ChannelHandler.Sharable
class KTimerAuthHandler(private val clientPool: ClientRegistry): ChannelInboundHandlerAdapter() {

    companion object{
        private val logger = KotlinLogging.logger {}
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is KTimerMessage<*>){
            val response = KTimerMessage.error("", "Invalid message type")
            logger.warn { "Received non-KTimerMessage: $msg" }
            ctx.writeAndFlush(response)
            return
        }

        if (clientPool.isRegistered(msg.clientId)){
            if (clientPool.getChannel(msg.clientId) == ctx.channel()){
                ctx.fireChannelRead(msg)
            }else{
                val response = KTimerMessage.error(msg.clientId, msg.messageId, "Client already registered with another channel")
                logger.warn { "Client ${msg.clientId} is already registered with another channel. Sending error response." }
                ctx.writeAndFlush(response)
            }
        }else{
            if (msg.type == KTimerMessageType.WELCOME) {
                clientPool.registerClient(msg.clientId, ctx.channel())
                logger.info { "Client ${msg.clientId} registered successfully." }
                ctx.writeAndFlush(KTimerMessage.welcome(msg.clientId, msg.messageId))
            } else {
                val response = KTimerMessage.error(msg.clientId, msg.messageId, "Authentication required")
                logger.warn { "Client ${msg.clientId} not registered. Sending error response." }
                ctx.writeAndFlush(response)
            }
        }
    }


}