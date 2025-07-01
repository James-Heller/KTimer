package space.jamestang.ktimer.message

import space.jamestang.ktimer.message.enums.MessageType

/**
 * The core message data module for KTimer.
 * This class serves as a base for all message types used in the KTimer application.
 *
 */
data class KTimerMessage(
    val version: String = "1.0",
    val type: MessageType,
    val messageId: String,
    val clientId: String,
    val timestamp: Long,
    val data: MessageData
)