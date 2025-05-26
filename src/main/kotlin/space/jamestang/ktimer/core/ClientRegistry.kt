package space.jamestang.ktimer.core

import io.netty.channel.Channel
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

class ClientRegistry {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val allChannels: ChannelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)

    private val clientChannels = ConcurrentHashMap<String, Channel>()

    fun registerClient(clientId: String, channel: Channel) {
        clientChannels[clientId] = channel
        allChannels.add(channel)

        channel.closeFuture().addListener {
            deregisterClient(clientId)
        }

        logger.info { "Client $clientId registered with channel ${channel.id()}" }
    }

    fun deregisterClient(clientId: String) {
        clientChannels.remove(clientId)?.also {
            logger.info {
                "Client $clientId deregistered from channel ${it.id()}"
            }
        }
    }

    fun getChannel(clientId: String): Channel? {
        return clientChannels[clientId]
    }

    fun isRegistered(clientId: String): Boolean {
        return clientChannels.containsKey(clientId)
    }

    fun getAllClients(): List<String> {
        return clientChannels.keys.toList()
    }
}