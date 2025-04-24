import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import space.jamestang.ktimer.data.KTimerMessage
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.Socket

fun main() {

    val data = KTimerMessage(KTimerMessage.MessageType.CANCEL_TASK, "AllTaken-1", Any())


    val bytes = jacksonObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).writeValueAsBytes(data)

    val fullData = ByteArrayOutputStream().use { baos ->
        DataOutputStream(baos).use { dos ->
            dos.writeInt(bytes.size)
            dos.write(bytes)
        }
        baos.toByteArray()
    }
    val socket = Socket("localhost", 4396).use { socket ->
        socket.outputStream.write(fullData)
    }

}