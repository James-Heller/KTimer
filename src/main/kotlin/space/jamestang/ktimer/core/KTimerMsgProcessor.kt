package space.jamestang.ktimer.core

import space.jamestang.ktimer.enum.KTimerMessageType
import space.jamestang.ktimer.message.KTimerMessage


object KTimerMsgProcessor {

    fun <T> processHeartBeat(msg: KTimerMessage<T>): KTimerMessage<Unit>{
        return KTimerMessage(
            clientId = msg.clientId,
            messageId = msg.messageId,
            type = KTimerMessageType.HEARTBEAT_RESPONSE,
            unit = null,
            payload = Unit
        )
    }


    fun <T> processTaskSend(msgDeliveryHandler: MessageDeliveryHandler, msg: KTimerMessage<T>): KTimerMessage<Unit>{
        msgDeliveryHandler.scheduleMessage(msg.clientId, msg.unit!!.amount, msg)

        return KTimerMessage(
            clientId = msg.clientId,
            messageId = msg.messageId,
            type = KTimerMessageType.TASK_RECEIVE,
            unit = null,
            payload = Unit
        )
    }
}