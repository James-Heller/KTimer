import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Slf4j
public class Client {

    private static DataInputStream input;
    private static DataOutputStream output;
    private static Socket socket;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        try {
            socket = new Socket("localhost", 4396) ;
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            sendMessage();

            int size = input.readInt();
            byte[] bytes = new byte[size];
            input.readFully(bytes);
            KTimerMessage payload = mapper.readValue(bytes, KTimerMessage.class);

            log.info(payload.toString());

            input.close();
            output.close();
            socket.close();

        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    public static void sendMessage(){
        KTimerMessage msg = new KTimerMessage(MessageType.HEARTBEAT, "PING", new Object());
        try {
            byte[] bytes = mapper.writeValueAsBytes(msg);
            output.writeInt(bytes.length);
            output.write(bytes);
            output.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
