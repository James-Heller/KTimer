package space.jamestang.ktimer.codec

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import mu.KotlinLogging
import space.jamestang.ktimer.message.KTimerMessage

class KTimerMessageDecoder: LengthFieldBasedFrameDecoder(
    16 * 1024 * 1024, // 16MB最大消息长度
    0,              // 长度字段偏移量
    4,              // 长度字段大小
    0,               // 长度调整
    4             // 跳过长度字段
) {

    val logger = KotlinLogging.logger {}

    private val mapper = jacksonObjectMapper().apply {
        val kotlin = KotlinModule.Builder().build()
        registerModule(kotlin)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf): KTimerMessage? {
        val frame = super.decode(ctx, input) as? ByteBuf ?: return null

        return try {
            val jsonBytes = ByteArray(frame.readableBytes())
            frame.readBytes(jsonBytes)
            mapper.readValue(jsonBytes, KTimerMessage::class.java)
        }catch (e: Exception){
            logger.error(e) { "Error deserializing message" }
            null
        } finally {
            frame.release()
        }
    }
}