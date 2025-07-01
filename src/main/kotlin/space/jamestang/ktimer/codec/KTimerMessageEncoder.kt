package space.jamestang.ktimer.codec

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.slf4j.Logger

import org.slf4j.LoggerFactory
import space.jamestang.ktimer.message.KTimerMessage

class KTimerMessageEncoder: MessageToByteEncoder<KTimerMessage>() {

    val logger: Logger = LoggerFactory.getLogger(KTimerMessageEncoder::class.java)

    private val mapper = jacksonObjectMapper().apply {
        findAndRegisterModules()
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    override fun encode(ctx: ChannelHandlerContext, msg: KTimerMessage, out: ByteBuf) {
        try {
            val jsonBytes = mapper.writeValueAsBytes(msg)
            out.writeInt(jsonBytes.size)
            out.writeBytes(jsonBytes)
        }catch (e: Exception) {
            logger.error("Error serializing message: {}", e.message)
        }
    }
}