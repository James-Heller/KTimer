package pers.jamestang.ktimer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import space.jamestang.ktimer.message.KTimerMessage
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.concurrent.TimeUnit

class AppTest {
    val socket = Socket("localhost", 8080)
    val mapper = jacksonObjectMapper()
    val dis = DataInputStream(socket.getInputStream())
    val dos = DataOutputStream(socket.getOutputStream())


    fun <T> readData(): KTimerMessage<T> {
        val length = dis.readInt()
        val bytes = ByteArray(length)
        dis.readFully(bytes)

        val msg = mapper.readValue<KTimerMessage<T>>(bytes)

        return msg
    }

    fun <T> writeData(msg: KTimerMessage<T>) {
        val bytes = mapper.writeValueAsBytes(msg)
        dos.writeInt(bytes.size)
        dos.write(bytes)
        dos.flush()
    }


    fun sendTask(){
        val taskMessage = KTimerMessage(
            clientId = "fabc4891",
            messageId = "task1",
            type = space.jamestang.ktimer.enum.KTimerMessageType.TASK_SEND,
            unit = KTimerMessage.TimeInfo(TimeUnit.SECONDS, 30),
            payload = "This is a test task"
        )
        writeData(taskMessage)
        val response = readData<Unit>()
    }

    fun waitTrigger(clientId: String){

    }
}


fun authAndSendTask(id: String, appTest: AppTest) {
    val welcomeMessage = KTimerMessage.welcome(id, "welcome1")
    appTest.writeData(welcomeMessage)
    val data = appTest.readData<Any>()
    println(data)

    val task = KTimerMessage(
        clientId = id,
        messageId = "task1",
        type = space.jamestang.ktimer.enum.KTimerMessageType.TASK_SEND,
        unit = KTimerMessage.TimeInfo(TimeUnit.SECONDS, 60),
        payload = "This is a test task"
    )

    appTest.writeData(task)
    println(appTest.readData<Any>())
}

fun authAndWaitTrigger(id: String, appTest: AppTest) {
    val welcomeMessage = KTimerMessage.welcome(id, "welcome1")
    appTest.writeData(welcomeMessage)
    val data = appTest.readData<Any>()
    println(data)

    val taskResp = appTest.readData<Any>()

    println("TRIGGER: $taskResp")
}

fun main() {

    val appTest = AppTest()
   val id = "fabc4891"




}