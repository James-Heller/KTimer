package space.jamestang.ktimer.core

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import space.jamestang.ktimer.data.KTimerMessage

class DebugHandler : ChannelDuplexHandler() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is ByteBuf -> {
                Constant.logger.info("Received ByteBuf: readableBytes=${msg.readableBytes()}")
            }
            is KTimerMessage -> {
                Constant.logger.debug("Decoded KTimerMessage: {}", msg)
            }
        }
        ctx.fireChannelRead(msg)
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        when (msg) {
            is KTimerMessage -> {
                Constant.logger.debug("Writing KTimerMessage: {}", msg)
            }
        }
        ctx.write(msg, promise)
    }
}