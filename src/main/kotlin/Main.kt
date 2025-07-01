import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import space.jamestang.ktimer.KTimer
import space.jamestang.ktimer.KTimerConfig

fun main(args: Array<String>) {

    val parser = ArgParser("KTimer")
    val port by parser.option(ArgType.Int, shortName = "p", description = "Port to listen").required()
    val bossThreads by parser.option(ArgType.Int, description = "Boss threads")
    val workerThreads by parser.option(ArgType.Int, description = "Worker threads")

    parser.parse(args)

    val config = KTimerConfig(
        port = port,
        bossThreads = bossThreads?: 1,
        workerThreads = workerThreads ?: (Runtime.getRuntime().availableProcessors() * 2),
    )


    val instance = KTimer(config)
    instance.start()
}