package space.jamestang.ktimer.data


/**
 * KTimerMessage is a data class that represents a message in the KTimer system.
 * @param clientId the UUID of the client. this value will generate and responsed when client trigger the register event.
 * @param type the type of the message. this value will be used to determine the type of the message.
 * @param taskId the unique ID of the task. this value will be used to identify the task. determine by the client.
 * @param context the context of the task. this value will be used to determine the context of the task. determine by the client.
 */
data class KTimerMessage(
    val clientId: String?,
    val type: MessageType,
    val taskId: String,
    val context: KTimerTaskContext?
){
    companion object{
        fun response(clientId: String): KTimerMessage{
            return KTimerMessage(
                clientId,
                MessageType.TASK_RECEIVED,
                "",
                null
            )
        }
    }
    enum class MessageType{
        CLIENT_REGISTER, SCHEDULE_TASK, TASK_TRIGGER, CANCEL_TASK, TASK_RECEIVED, HEARTBEAT, ERROR
    }
}