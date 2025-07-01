package space.jamestang.ktimer

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import space.jamestang.ktimer.codec.KTimerMessageDecoder
import space.jamestang.ktimer.codec.KTimerMessageEncoder
import space.jamestang.ktimer.connectionManager.ConnectionManager
import space.jamestang.ktimer.messageHandler.TimerMessageHandler
import java.util.concurrent.TimeUnit

class KTimer(
    private val config: KTimerConfig
){

    private val logger = KotlinLogging.logger {}
    private lateinit var bossGroup: EventLoopGroup
    private lateinit var workerGroup: EventLoopGroup
    private lateinit var serverBootstrap: ServerBootstrap
    private var channelFuture: ChannelFuture? = null


    private val connectionManager = ConnectionManager()
    private val messageHandler = TimerMessageHandler(connectionManager)


    fun start() {


        bossGroup = MultiThreadIoEventLoopGroup(
            config.bossThreads,
            NioIoHandler.newFactory()
        )

        workerGroup = MultiThreadIoEventLoopGroup(
            config.workerThreads,
            NioIoHandler.newFactory()
        )

        serverBootstrap = ServerBootstrap().apply {
            group(bossGroup, workerGroup)
            channel(NioServerSocketChannel::class.java)


            option(ChannelOption.SO_BACKLOG, config.soBacklog)
            option(ChannelOption.SO_REUSEADDR, true)


            childOption(ChannelOption.SO_KEEPALIVE, true)
            childOption(ChannelOption.TCP_NODELAY, true)
            childOption(ChannelOption.SO_RCVBUF, config.soRcvbuf)
            childOption(ChannelOption.SO_SNDBUF, config.soSndbuf)

            childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().apply {

                        addLast("idleHandler", IdleStateHandler(
                            config.readerIdleTimeSeconds.toLong(),
                            config.writerIdleTimeSeconds.toLong(),
                            config.allIdleTimeSeconds.toLong(),
                            TimeUnit.SECONDS
                        )
                        )


                        addLast("decoder", KTimerMessageDecoder())
                        addLast("encoder", KTimerMessageEncoder())


                        addLast("messageHandler", messageHandler)


                        addLast("idleEventHandler", IdleEventHandler())
                    }
                }
            })
        }

        try {
            channelFuture = serverBootstrap.bind(config.port).sync()
            logger.info("Timer server started successfully")
            logger.info("Listening on port: ${config.port}")
            logger.info("Boss threads: ${config.bossThreads}, Worker threads: ${config.workerThreads}")
        } catch (e: Exception) {
            logger.error("Failed to start timer server", e)
            stop()
            throw e
        }
    }


    fun stop() {
        try {
            channelFuture?.channel()?.close()?.sync()
        } catch (e: Exception) {
            logger.error("Error closing server channel", e)
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
            logger.info("Timer server stopped")
        }
    }


    fun getConnectionManager(): ConnectionManager = connectionManager


    private class IdleEventHandler : ChannelInboundHandlerAdapter() {
        private val logger = LoggerFactory.getLogger(IdleEventHandler::class.java)

        override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
            if (evt is IdleStateEvent) {
                when (evt.state()) {
                    IdleState.READER_IDLE -> {
                        logger.warn("Channel read idle: ${ctx.channel().remoteAddress()}")
                        ctx.close()
                    }
                    IdleState.WRITER_IDLE -> {
                        logger.debug("Channel write idle: {}", ctx.channel().remoteAddress())
                    }
                    IdleState.ALL_IDLE -> {
                        logger.warn("Channel all idle: ${ctx.channel().remoteAddress()}")
                        ctx.close()
                    }
                }
            }
            super.userEventTriggered(ctx, evt)
        }
    }
}