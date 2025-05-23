package space.jamestang.ktimer.message

import space.jamestang.ktimer.enum.KTimerMessageType
import java.util.concurrent.TimeUnit

data class KTimerMessage<T>(
    val clientId: String,
    val messageId: String,
    val type: KTimerMessageType,
    val unit: TimeInfo?,
    val payload: T
){
    data class  TimeInfo(
        val unit: TimeUnit,
        val amount: Long
    )
}