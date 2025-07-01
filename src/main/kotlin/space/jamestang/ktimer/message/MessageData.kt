package space.jamestang.ktimer.message

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AckData::class, name = "ack_data"),
    JsonSubTypes.Type(value = ClientMetadata::class, name = "client_metadata"),
    JsonSubTypes.Type(value = ClientRegisterData::class, name = "client_register_data"),
    JsonSubTypes.Type(value = ErrorData::class, name = "error_data"),
    JsonSubTypes.Type(value = HeartbeatData::class, name = "heartbeat_data"),
    JsonSubTypes.Type(value = KTimerMessage::class, name = "ktimer_message"),
    JsonSubTypes.Type(value = TimerCallbackData::class, name = "timer_callback_data"),
    JsonSubTypes.Type(value = TimerCancelData::class, name = "timer_cancel_data"),
    JsonSubTypes.Type(value = TimerRegisterData::class, name = "timer_register_data")
)
sealed class MessageData()
