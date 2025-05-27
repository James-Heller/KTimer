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


    companion object{
        fun error(
            clientId: String = "server",
            messageId: String = "error",
            payload: String = "An error occurred"
        ): KTimerMessage<String> {
            return KTimerMessage(
                clientId = clientId,
                messageId = messageId,
                type = KTimerMessageType.ERROR,
                unit = null,
                payload = payload
            )
        }

        fun welcome(clientId: String = "server", messageId: String = "welcome"): KTimerMessage<Unit> {
            return KTimerMessage(
                clientId = clientId,
                messageId = messageId,
                type = KTimerMessageType.WELCOME,
                unit = null,
                payload = Unit
            )
        }
    }
}