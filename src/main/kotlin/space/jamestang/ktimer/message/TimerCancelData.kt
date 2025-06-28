package space.jamestang.ktimer.message

data class TimerCancelData(
    val timerId: String,
    val reason: String? = null,
    val force: Boolean = false,
): MessageData()
