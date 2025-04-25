package space.jamestang.ktimer.core

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import space.jamestang.ktimer.ConnectionPool
import space.jamestang.ktimer.data.KTimerMessage

class KTimerHandler(private val connectionPool: ConnectionPool): SimpleChannelInboundHandler<KTimerMessage>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        connectionPool.addConnection(ctx.name(), ctx.channel())
        super.channelActive(ctx)
    }
    override fun channelRead0(ctx: ChannelHandlerContext, msg: KTimerMessage) {
        // Handle the incoming message
        if (msg != null) {

            if (msg.type == KTimerMessage.MessageType.CLIENT_REGISTER){
                Constant.logger.info("Done.")
            }
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        connectionPool.removeConnection(ctx.name())
    }
}