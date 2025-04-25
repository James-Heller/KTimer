package space.jamestang.ktimer.codec

import com.fasterxml.jackson.module.kotlin.readValue
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import space.jamestang.ktimer.core.Constant
import space.jamestang.ktimer.data.KTimerMessage

class KTimerMessageDecoder: ByteToMessageDecoder() {


    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: List<Any>) {

        val readableBytes = msg.readableBytes()
        if (readableBytes > 0){
            val bytes = ByteArray(readableBytes)
            msg.readBytes(bytes)

            try {
                val message = Constant.mapper.readValue<KTimerMessage>(bytes)

                out as MutableList<Any>
                out.add(message)
            }catch (e: Exception){
                if (Constant.logger.isDebugEnabled){
                    e.printStackTrace()
                }
                Constant.logger.error("Failed to decode message: ${e.message}")
            }
        }
    }
}