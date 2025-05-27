package space.jamestang.ktimer.core

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import space.jamestang.ktimer.codec.KTimerMessageDecoder
import space.jamestang.ktimer.codec.KTimerMessageEncoder
import space.jamestang.ktimer.message.KTimerMessage


class KTimerChannelInitializer(
    private val messageEncoder: (KTimerMessage<Any>) -> ByteArray,
    private val messageDecoder: (ByteArray) -> KTimerMessage<Any>,
    clientRegistry: ClientRegistry
): ChannelInitializer<SocketChannel>(), AutoCloseable {

    private val authHandler = KTimerAuthHandler(clientRegistry)
    private val messageHandler = KTimerMessageHandler(clientRegistry)

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().apply {
            addLast(LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
            addLast(LengthFieldPrepender(4))
            addLast(KTimerMessageEncoder(messageEncoder))
            addLast(KTimerMessageDecoder(messageDecoder))
            addLast(authHandler)
            addLast(messageHandler)
        }

    }

    override fun close() {
        messageHandler.close()
    }
}