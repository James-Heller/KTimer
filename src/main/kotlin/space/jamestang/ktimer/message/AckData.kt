package space.jamestang.ktimer.message


data class AckData(
    val originalMessageId: String,
    val status: AckStatus,
    val code: Int = 200,
    val message: String,
    val details: Map<String, Any>? = null
) : MessageData()

enum class AckStatus {
    SUCCESS, FAILED
}