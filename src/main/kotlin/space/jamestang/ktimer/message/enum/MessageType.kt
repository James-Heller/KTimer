package space.jamestang.ktimer.message.enum

enum class MessageType {
    CLIENT_REGISTER, CLIENT_UNREGISTER,
    TIMER_REGISTER, TIMER_CANCEL,
    TIMER_CALLBACK, HEARTBEAT,
    ACK, ERROR
}