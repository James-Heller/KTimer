package space.jamestang.ktimer.message

data class HeartbeatData(
    val status: String = "healthy",
    val activeTimers: Int = 0,
    val processedCount: Long = 0L,
    val uptime: Long,
    val systemInfo: SystemInfo? = null
) : MessageData()

data class SystemInfo(
    val cpuUsage: Double,
    val memoryUsage: Double,
    val diskUsage: Double
)