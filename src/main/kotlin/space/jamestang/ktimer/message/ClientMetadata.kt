package space.jamestang.ktimer.message

data class ClientMetadata(
    val hostname: String,
    val ip: String,
    val port: Int,
    val environment: String
): MessageData()
