package space.jamestang.ktimer.codec

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.jamestang.ktimer.message.KTimerMessage

class KTimerMessageDecoder: LengthFieldBasedFrameDecoder(
    16 * 1024 * 1024,
    0,
    4,
    0,
    4
) {

    val logger: Logger = LoggerFactory.getLogger(KTimerMessageDecoder::class.java)

    private val mapper = jacksonObjectMapper().apply {
        findAndRegisterModules()
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf): KTimerMessage? {
        val frame = super.decode(ctx, input) as? ByteBuf ?: return null

        return try {
            val jsonBytes = ByteArray(frame.readableBytes())
            frame.readBytes(jsonBytes)
            mapper.readValue(jsonBytes, KTimerMessage::class.java)
        }catch (e: Exception){
            logger.error("Error deserializing message: {}", e.message)
            null
        } finally {
            frame.release()
        }
    }
}