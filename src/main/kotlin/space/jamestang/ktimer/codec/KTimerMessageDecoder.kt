package space.jamestang.ktimer.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import mu.KotlinLogging
import space.jamestang.ktimer.message.KTimerMessage

class KTimerMessageDecoder(private val messageDecoder: (ByteArray) -> KTimerMessage<Any>) : ByteToMessageDecoder() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        try {
            val byteArray = ByteArray(msg.readableBytes())
            msg.readBytes(byteArray)
            val message = messageDecoder.invoke(byteArray)
            out.add(message)
        } catch (e: Exception) {
            logger.error { "Failed to deserialize message: ${e.message}" }
        }
    }
}