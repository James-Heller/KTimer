package space.jamestang.ktimer.data

data class KTimerMessage(
    val type: MessageType,
    val taskId: String,
    val context: Any
){
    enum class MessageType{
        CLIENT_REGISTER, SCHEDULE_TASK, TASK_TRIGGER, CANCEL_TASK
    }
}