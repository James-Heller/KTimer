package space.jamestang.ktimer.core

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import space.jamestang.ktimer.data.KTimerMessage

class KTimerHandler: SimpleChannelInboundHandler<KTimerMessage>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)

    }
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: KTimerMessage?) {
        // Handle the incoming message
        if (msg != null) {

            Constant.logger.info(String.format("Received msg: %s", msg))
        }
    }
}