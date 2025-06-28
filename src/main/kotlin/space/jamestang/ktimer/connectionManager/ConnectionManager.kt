package space.jamestang.ktimer.connectionManager

import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.util.AttributeKey
import mu.KotlinLogging
import space.jamestang.ktimer.connectionManager.enum.ConnectionStatus
import space.jamestang.ktimer.message.KTimerMessage
import space.jamestang.ktimer.message.MessageBuilder
import java.util.concurrent.ConcurrentHashMap

class ConnectionManager {
    private val connections = ConcurrentHashMap<String, ClientConnection>()
    private val clientChannels = ConcurrentHashMap<String, String>() // clientId -> connectionId
    private val logger = KotlinLogging.logger {}

    /**
     * 注册新连接
     */
    fun registerConnection(channel: Channel): String {
        val connectionId = generateConnectionId()
        val connection = ClientConnection(
            connectionId = connectionId,
            channel = channel
        )

        connections[connectionId] = connection

        // 在Channel中存储连接ID
        channel.attr(CONNECTION_ID_KEY).set(connectionId)

        logger.info("New connection registered: $connectionId from ${channel.remoteAddress()}")
        return connectionId
    }

    /**
     * 绑定客户端ID
     */
    fun bindClientId(connectionId: String, clientId: String): Boolean {
        val connection = connections[connectionId] ?: return false

        // 检查clientId是否已被其他连接使用
        val existingConnectionId = clientChannels[clientId]
        if (existingConnectionId != null && existingConnectionId != connectionId) {
            // 处理重复clientId的情况
            handleDuplicateClientId(clientId, existingConnectionId, connectionId)
        }

        connection.copy(clientId = clientId, status = ConnectionStatus.REGISTERED).also {
            connections[connectionId] = it
        }
        clientChannels[clientId] = connectionId

        logger.info("Client $clientId bound to connection $connectionId")
        return true
    }

    /**
     * 移除连接
     */
    fun removeConnection(connectionId: String) {
        val connection = connections.remove(connectionId)
        if (connection != null) {
            connection.clientId?.let { clientChannels.remove(it) }
            logger.info("Connection removed: $connectionId, client: ${connection.clientId}")
        }
    }

    /**
     * 根据客户端ID获取连接
     */
    fun getConnectionByClientId(clientId: String): ClientConnection? {
        val connectionId = clientChannels[clientId] ?: return null
        return connections[connectionId]
    }

    /**
     * 根据连接ID获取连接
     */
    fun getConnection(connectionId: String): ClientConnection? {
        return connections[connectionId]
    }

    /**
     * 更新心跳时间
     */
    fun updateHeartbeat(connectionId: String) {
        connections[connectionId]?.let {
            connections[connectionId] = it.copy(lastHeartbeat = System.currentTimeMillis())
        }
    }

    /**
     * 获取所有活跃连接
     */
    fun getAllActiveConnections(): List<ClientConnection> {
        return connections.values.filter { it.status != ConnectionStatus.DISCONNECTED }
    }

    /**
     * 发送消息到指定客户端
     */
    fun sendToClient(clientId: String, message: KTimerMessage): Boolean {
        val connection = getConnectionByClientId(clientId)
        return if (connection?.channel?.isActive == true) {
            connection.channel.writeAndFlush(message)
            true
        } else {
            logger.warn("Cannot send message to client $clientId: connection not active")
            false
        }
    }

    private fun generateConnectionId(): String {
        return "conn_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    private fun handleDuplicateClientId(clientId: String, oldConnectionId: String, newConnectionId: String) {
        logger.warn("Duplicate clientId $clientId detected. Old: $oldConnectionId, New: $newConnectionId")

        // 断开旧连接
        val oldConnection = connections[oldConnectionId]
        if (oldConnection?.channel?.isActive == true) {
            val errorMessage = MessageBuilder.createError(
                clientId = clientId,
                errorCode = "DUPLICATE_CLIENT_ID",
                errorMessage = "Client ID is already in use by another connection"
            )
            oldConnection.channel.writeAndFlush(errorMessage).addListener(ChannelFutureListener.CLOSE)
        }

        removeConnection(oldConnectionId)
    }

    companion object {
        val CONNECTION_ID_KEY: AttributeKey<String> = AttributeKey.valueOf("connectionId")
    }
}