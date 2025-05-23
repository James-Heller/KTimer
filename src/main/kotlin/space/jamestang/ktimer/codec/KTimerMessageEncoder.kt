package space.jamestang.ktimer.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import mu.KotlinLogging
import space.jamestang.ktimer.message.KTimerMessage

class KTimerMessageEncoder(private val messageEncoder: (KTimerMessage<Any>) -> ByteArray): MessageToByteEncoder<KTimerMessage<Any>>() {

    companion object{
        private val logger = KotlinLogging.logger{}
    }

    override fun encode(ctx: ChannelHandlerContext, msg: KTimerMessage<Any>, out: ByteBuf) {

        try {
            val byteArray = messageEncoder.invoke(msg)
            out.writeBytes(byteArray)

        }catch (e: Exception){
            logger.error { "Failed to serialize message: ${e.message}" }
        }
    }
}