package space.jamestang.ktimer.core

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.IoHandlerFactory
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioServerSocketChannel
import space.jamestang.ktimer.message.KTimerMessage
import java.util.function.Function

class KTimer() {

    private val nioHandlerFactory: IoHandlerFactory = NioIoHandler.newFactory()
    private val bossGroup = MultiThreadIoEventLoopGroup(nioHandlerFactory)
    private val workerGroup = MultiThreadIoEventLoopGroup(nioHandlerFactory)

    internal lateinit var messageEncoder: (KTimerMessage<Any>) -> ByteArray
    internal lateinit var messageDecoder: (ByteArray) -> KTimerMessage<Any>

    fun strat(){

        if (!::messageDecoder.isInitialized || !::messageEncoder.isInitialized) {
            throw IllegalStateException("messageEncoder or messageDecoder is not initialized")
        }

        val server = ServerBootstrap()
        server.apply {
            group(bossGroup, workerGroup)
            channel(NioServerSocketChannel::class.java)
            childOption(ChannelOption.SO_KEEPALIVE, true)
            childHandler(KTimerChannelInitializer(messageEncoder, messageDecoder))

        }

        val channel = server.bind(8080).sync().channel()
    }

}

