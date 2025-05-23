import space.jamestang.ktimer.core.KTimer
import space.jamestang.ktimer.enum.KTimerMessageType
import space.jamestang.ktimer.message.KTimerMessage

fun main() {

    KTimer().apply {
        messageEncoder = { it.toString().toByteArray() }
        messageDecoder = { KTimerMessage<Any>("", "", KTimerMessageType.TASK_RECEIVE, null, Any()) }
    }.strat()
}