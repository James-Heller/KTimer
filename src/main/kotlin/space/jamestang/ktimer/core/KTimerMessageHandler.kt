package space.jamestang.ktimer.core

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import space.jamestang.ktimer.message.KTimerMessage

class KTimerMessageHandler: SimpleChannelInboundHandler<KTimerMessage<Any>>() {

    companion object{
        private val logger = mu.KotlinLogging.logger {}
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: KTimerMessage<Any>) {
        logger.info { msg.toString() }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        logger.error { "Exception caught: ${cause?.message}" }
        ctx?.close()
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        logger.info { "Channel inactive" }
        ctx?.close()
    }
}