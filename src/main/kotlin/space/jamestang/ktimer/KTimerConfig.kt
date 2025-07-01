package space.jamestang.ktimer

data class KTimerConfig(
    val port: Int = 8080,
    val bossThreads: Int = 1,
    val workerThreads: Int = Runtime.getRuntime().availableProcessors() * 2,
    val soBacklog: Int = 1024,
    val soRcvbuf: Int = 65536,
    val soSndbuf: Int = 65536,
    val readerIdleTimeSeconds: Int = 60,
    val writerIdleTimeSeconds: Int = 30,
    val allIdleTimeSeconds: Int = 0
)
