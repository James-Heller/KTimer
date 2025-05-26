package space.jamestang.ktimer.enum

enum class KTimerMessageType {
    APPLY_CODE,
    APPLY_CODE_SUCCESS,
    HEARTBEAT,
    HEARTBEAT_RESPONSE,
    TASK_SEND,
    TASK_RECEIVE,
    TASK_TRIGGER,
    REGISTER,
    REGISTER_SUCCESS,
    REGISTER_FAIL,
    UNREGISTER,
    ERROR
}