package space.jamestang.ktimer.message


data class TimerCallbackData(
    val timerId: String,
    val originalTimestamp: Long,
    val executeTimestamp: Long,
    val attempt: Int = 1,
    val payload: Any,
    val classInfo: String
) : MessageData()
