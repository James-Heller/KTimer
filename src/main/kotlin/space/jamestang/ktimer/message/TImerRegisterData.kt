package space.jamestang.ktimer.message

import space.jamestang.ktimer.message.enums.TimerPriority

/**
 * 定时器注册数据
 */
data class TimerRegisterData(
    val timerId: String,
    val delayMillis: Long,
    val executeAt: Long? = null,
    val repeatInterval: Long = 0L,
    val maxRetries: Int = 3,
    val priority: TimerPriority = TimerPriority.NORMAL,
    val payload: Any,
    val classInfo: String,
    val tags: Map<String, String>? = null
) : MessageData()


