package space.jamestang.ktimer.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import space.jamestang.ktimer.core.Constant
import space.jamestang.ktimer.data.KTimerMessage

class KTimerMessageEncoder: MessageToByteEncoder<KTimerMessage>() {
    override fun encode(ctx: ChannelHandlerContext, msg: KTimerMessage, out: ByteBuf) {
        try {
            val bytes = Constant.mapper.writeValueAsBytes(msg)
            out.writeBytes(bytes)

        }catch (e: Exception){
            if (Constant.logger.isDebugEnabled){
                e.printStackTrace()
            }
            Constant.logger.error(e.message)
        }
    }


}