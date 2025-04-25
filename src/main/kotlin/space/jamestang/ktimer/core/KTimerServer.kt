package space.jamestang.ktimer.core

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.util.HashedWheelTimer
import space.jamestang.ktimer.ConnectionPool
import space.jamestang.ktimer.codec.KTimerMessageDecoder
import space.jamestang.ktimer.codec.KTimerMessageEncoder


class KTimerServer(private val port: Int) {

    private val timer: HashedWheelTimer = HashedWheelTimer()
    private val ioFactory = NioIoHandler.newFactory()
    private val bossEnvLoop = MultiThreadIoEventLoopGroup(1, ioFactory)
    private val workerGroup = MultiThreadIoEventLoopGroup(ioFactory)
    private val connectionPool = ConnectionPool()


    fun start() {
        try {
            val server = ServerBootstrap()
            server.apply {
                group(bossEnvLoop, workerGroup)
                channel(NioServerSocketChannel::class.java)
                childHandler(constructChannelHandler())
                option(ChannelOption.SO_BACKLOG, 128)
                childOption(ChannelOption.SO_KEEPALIVE, true)
            }

            server.bind(port).sync()
        }catch (e: Exception){
            if (Constant.logger.isDebugEnabled){
                e.printStackTrace()
            }
            Constant.logger.error(e.message)
        }


    }



    private fun constructChannelHandler(): ChannelInitializer<SocketChannel> {
        val handler = object : ChannelInitializer<SocketChannel>(){
            override fun initChannel(ch: SocketChannel) {
                val pipeline = ch.pipeline()
                pipeline.apply {
                    addLast(DebugHandler())
                    // 入站处理器
                    addLast(LengthFieldBasedFrameDecoder(1024*1024*1, 0, 4, 0, 4))
                    addLast(KTimerMessageDecoder())

                    // 出站处理器
                    addLast(LengthFieldPrepender(4, false))  // 再添加长度字段
                    addLast(KTimerMessageEncoder())     // 先编码消息


                    // 业务处理器
                    addLast(KTimerHandler(connectionPool))

                }
            }
        }

        return handler
    }
}