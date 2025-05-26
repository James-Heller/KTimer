package pers.jamestang.ktimer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import space.jamestang.ktimer.core.KTimer
import space.jamestang.ktimer.enum.KTimerMessageType
import space.jamestang.ktimer.message.KTimerMessage
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTest {

    var server: KTimer = KTimer()
    private val mapper = jacksonObjectMapper()

    lateinit var client: Socket
    lateinit var dataInputStream: DataInputStream
    lateinit var dataOutputStream: DataOutputStream

    init {
        server.messageDecoder = { bytes -> mapper.readValue<KTimerMessage<Any>>(bytes) }
        server.messageEncoder = { message -> mapper.writeValueAsBytes(message) }
        server.strat()
    }



    @BeforeTest
    fun setup(){

        client = Socket("localhost", 8080)
        dataInputStream = DataInputStream(client.getInputStream())
        dataOutputStream = DataOutputStream(client.getOutputStream())
    }



    @Test
    fun heartBeat(){

        // Send a heartbeat message
        val heartbeatMessage = KTimerMessage<Unit>(
            clientId = "client1",
            messageId = "msg1",
            type = KTimerMessageType.HEARTBEAT,
            unit = null,
            payload = Unit
        )

        sendData(heartbeatMessage)

        // Receive the response
        val response: KTimerMessage<Unit> = receiveData()
        assertEquals(KTimerMessageType.HEARTBEAT_RESPONSE, response.type, "Response type should be HEARTBEAT_RESPONSE")
    }

    @Test
    fun applyCode() {

        // Send an apply code message
        val applyCodeMessage = KTimerMessage<Unit>(
            clientId = "client1",
            messageId = "msg1",
            type = KTimerMessageType.APPLY_CODE,
            unit = null,
            payload = Unit
        )

        sendData(applyCodeMessage)

        // Receive the response
        val response: KTimerMessage<String> = receiveData()
        assertEquals(KTimerMessageType.APPLY_CODE_SUCCESS, response.type, "Response type should be APPLY_CODE_SUCCESS")
        assertEquals("msg1", response.messageId, "Message ID should match the request")
    }

    @Test
    fun auth() {

        // Send an apply code message
        val applyCodeMessage = KTimerMessage<Unit>(
            clientId = "client1",
            messageId = "msg2",
            type = KTimerMessageType.AUTH_REQUEST,
            unit = null,
            payload = Unit
        )

        sendData(applyCodeMessage)

        // Receive the response
        val response: KTimerMessage<String> = receiveData()
        assertEquals(KTimerMessageType.AUTH_SUCCESS, response.type, "Response type should be APPLY_CODE_SUCCESS")
        assertEquals("msg2", response.messageId, "Message ID should match the request")
    }


    @Test
    fun sendTaskWithNoAuth(){
        val task = KTimerMessage<String>(
            clientId = "client1",
            messageId = "task1",
            type = KTimerMessageType.TASK_SEND,
            unit = KTimerMessage.TimeInfo(TimeUnit.SECONDS, 20),
            payload = "This is a test task"
        )

        sendData(task)

        val response: KTimerMessage<String> = receiveData()
        assertEquals(KTimerMessageType.ERROR, response.type, "Response type should be ERROR")
    }

    @Test
    fun sendTaskWithAuthAndWaitTRIGGER(){
        val authMessage = KTimerMessage<Unit>(
            clientId = "client1",
            messageId = "auth1",
            type = KTimerMessageType.AUTH_REQUEST,
            unit = null,
            payload = Unit
        )

        sendData(authMessage)

        val authResponse: KTimerMessage<String> = receiveData()
        assertEquals(KTimerMessageType.AUTH_SUCCESS, authResponse.type, "Response type should be AUTH_SUCCESS")

        val task = KTimerMessage<String>(
            clientId = "client1",
            messageId = "task1",
            type = KTimerMessageType.TASK_SEND,
            unit = KTimerMessage.TimeInfo(TimeUnit.SECONDS, 20),
            payload = "This is a test task"
        )

        sendData(task)
        val response: KTimerMessage<Unit> = receiveData()
        assertEquals(KTimerMessageType.TASK_RECEIVE, response.type, "Response type should be TASK_RECEIVE")


        Thread.sleep(20000)

        val triggered = receiveData<String>()

        assertEquals(KTimerMessageType.TASK_TRIGGER, triggered.type, "Response type should be TASK_TRIGGER")
        println(triggered)
    }


    @AfterTest
    fun tearDown() {
        dataInputStream.close()
        dataOutputStream.close()
        client.close()
        server.stop()
        Thread.sleep(1000)
    }



    private fun <T> sendData(message: KTimerMessage<T>){
        val bytes = mapper.writeValueAsBytes(message)
        val lengthPadding = bytes.size
        dataOutputStream.writeInt(lengthPadding)
        dataOutputStream.write(bytes)
        dataOutputStream.flush()
    }

    private fun <T> receiveData(timeoutMillis: Int = 5000): KTimerMessage<T> {
        // 设置读取超时时间
        val originalTimeout = client.soTimeout
        try {
            client.soTimeout = timeoutMillis
            val lengthPadding = dataInputStream.readInt()
            val bytes = ByteArray(lengthPadding)
            dataInputStream.readFully(bytes)
            return mapper.readValue(bytes)
        } catch (e: java.net.SocketTimeoutException) {
            throw RuntimeException("接收数据超时，等待超过 $timeoutMillis 毫秒", e)
        } finally {
            // 恢复原始超时设置
            client.soTimeout = originalTimeout
        }
    }
}