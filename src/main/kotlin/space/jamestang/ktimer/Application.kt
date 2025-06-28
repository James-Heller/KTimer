package space.jamestang.ktimer

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.concurrent.DefaultThreadFactory
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import space.jamestang.ktimer.codec.KTimerMessageDecoder
import space.jamestang.ktimer.codec.KTimerMessageEncoder
import space.jamestang.ktimer.connectionManager.ConnectionManager
import space.jamestang.ktimer.messageHandler.TimerMessageHandler
import java.util.concurrent.TimeUnit

class Application(
    private val config: ApplicationConfig
){

    private val logger = KotlinLogging.logger {}
    private lateinit var bossGroup: EventLoopGroup
    private lateinit var workerGroup: EventLoopGroup
    private lateinit var serverBootstrap: ServerBootstrap
    private var channelFuture: ChannelFuture? = null

    // 核心组件
    private val connectionManager = ConnectionManager()
    private val messageHandler = TimerMessageHandler(connectionManager)

    /**
     * 启动服务器
     */
    fun start() {

        val bossThreadFactory = ThreadFactoryBuilder()
            .setNameFormat("timer-boss-%d")
            .setDaemon(false)
            .build()

        val workerThreadFactory = ThreadFactoryBuilder()
            .setNameFormat("timer-worker-%d")
            .setDaemon(false)
            .build()

        // 使用新的API创建EventLoopGroup
        bossGroup = MultiThreadIoEventLoopGroup(
            config.bossThreads,
            VirtualThreadFactory(bossThreadFactory),
            NioIoHandler.newFactory()
        )

        workerGroup = MultiThreadIoEventLoopGroup(
            config.workerThreads,
            workerThreadFactory,
            NioIoHandler.newFactory()
        )

        serverBootstrap = ServerBootstrap().apply {
            group(bossGroup, workerGroup)
            channel(NioServerSocketChannel::class.java)

            // 服务端选项
            option(ChannelOption.SO_BACKLOG, config.soBacklog)
            option(ChannelOption.SO_REUSEADDR, true)

            // 子通道选项
            childOption(ChannelOption.SO_KEEPALIVE, true)
            childOption(ChannelOption.TCP_NODELAY, true)
            childOption(ChannelOption.SO_RCVBUF, config.soRcvbuf)
            childOption(ChannelOption.SO_SNDBUF, config.soSndbuf)

            childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().apply {
                        // 空闲检测
                        addLast("idleHandler", IdleStateHandler(
                            config.readerIdleTimeSeconds.toLong(),
                            config.writerIdleTimeSeconds.toLong(),
                            config.allIdleTimeSeconds.toLong(),
                            TimeUnit.SECONDS
                        )
                        )

                        // 消息编解码
                        addLast("decoder", KTimerMessageDecoder())
                        addLast("encoder", KTimerMessageEncoder())

                        // 业务处理
                        addLast("messageHandler", messageHandler)

                        // 空闲处理
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

    /**
     * 停止服务器
     */
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

    /**
     * 获取连接管理器
     */
    fun getConnectionManager(): ConnectionManager = connectionManager

    /**
     * 空闲事件处理器
     */
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