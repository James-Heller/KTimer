package space.jamestang.ktimer.message

data class ClientRegisterData(
    val instanceId: String,
    val serviceName: String,
    val version: String,
    val capabilities: List<String>,
    val metadata: ClientMetadata
): MessageData()
