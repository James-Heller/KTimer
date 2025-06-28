package space.jamestang.ktimer.message


data class ErrorData(
    val originalMessageId: String? = null,
    val errorCode: String,
    val errorMessage: String,
    val details: Map<String, Any>? = null,
    val suggestions: List<String>? = null
) : MessageData()