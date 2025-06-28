package space.jamestang.ktimer.connectionManager

import io.netty.channel.Channel
import space.jamestang.ktimer.connectionManager.enum.ConnectionStatus

data class ClientConnection(
    val connectionId: String,
    val clientId: String? = null,
    val channel: Channel,
    val connectTime: Long = System.currentTimeMillis(),
    var lastHeartbeat: Long = System.currentTimeMillis(),
    var status: ConnectionStatus = ConnectionStatus.CONNECTED,
    val metadata: MutableMap<String, Any> = mutableMapOf()
)
