package pers.jamestang.ktimer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import space.jamestang.ktimer.core.KTimer
import space.jamestang.ktimer.enum.KTimerMessageType
import space.jamestang.ktimer.message.KTimerMessage
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTest {

    lateinit var server: KTimer
    private val mapper = jacksonObjectMapper()

    lateinit var client: Socket
    lateinit var dataInputStream: DataInputStream
    lateinit var dataOutputStream: DataOutputStream




    @BeforeTest
    fun setup(){
        server = KTimer()
        server.messageDecoder = { bytes -> mapper.readValue<KTimerMessage<Any>>(bytes) }
        server.messageEncoder = { message -> mapper.writeValueAsBytes(message) }
        server.strat()

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
        println(response)
        assertEquals(KTimerMessageType.HEARTBEAT_RESPONSE, response.type, "Response type should be HEARTBEAT_RESPONSE")
    }


    @AfterTest
    fun tearDown() {
        dataInputStream.close()
        dataOutputStream.close()
        client.close()

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