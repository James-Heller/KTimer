package space.jamestang.ktimer.core

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.IoHandlerFactory
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioServerSocketChannel
import space.jamestang.ktimer.message.KTimerMessage

class KTimer() {

    companion object{
        val logger = mu.KotlinLogging.logger("KTimer")
    }

    private val nioHandlerFactory: IoHandlerFactory = NioIoHandler.newFactory()
    private val bossGroup = MultiThreadIoEventLoopGroup(nioHandlerFactory)
    private val workerGroup = MultiThreadIoEventLoopGroup(nioHandlerFactory)
    private var iNetPort = 8080

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

        val promise = server.bind(iNetPort).sync()

        if (promise.isSuccess){
            logger.info { "KTimer start successfully at port $iNetPort" }
        } else {
            throw RuntimeException("Failed to start KTimer server: ${promise.cause()?.message}")
        }
    }

}

